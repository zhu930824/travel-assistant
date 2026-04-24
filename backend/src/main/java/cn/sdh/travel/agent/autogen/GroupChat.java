package cn.sdh.travel.agent.autogen;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 群聊管理
 * 管理多个 Agent 之间的对话协作
 *
 * 核心流程:
 * 1. 初始化对话状态和所有 Agent
 * 2. 循环执行对话轮次
 * 3. 每轮选择下一个发言者
 * 4. 检查终止条件
 * 5. 生成最终结果
 */
@Slf4j
public class GroupChat {

    @Getter
    private final List<ConversableAgent> agents = new ArrayList<>();

    private final GroupChatManager manager;
    private final TerminationCondition terminationCondition;
    private final ConversationState state;

    public GroupChat(List<ConversableAgent> agents, GroupChatManager manager,
                      TerminationCondition terminationCondition) {
        this.agents.addAll(agents);
        this.manager = manager;
        this.terminationCondition = terminationCondition;
        this.state = new ConversationState();
    }

    /**
     * 执行群聊
     */
    public GroupChatResult run(String initialMessage) {
        log.info("【AutoGen群聊】开始执行，共 {} 个 Agent", agents.size());
        reset();

        // 添加系统消息
        state.addMessage(ConversationMessage.system("对话开始: " + initialMessage));

        // 找到 UserProxyAgent 作为起始发言者
        ConversableAgent currentSpeaker = findUserProxy();
        if (currentSpeaker == null) {
            currentSpeaker = agents.getFirst();
        }

        // 初始消息
        String currentMessage = initialMessage;

        // 主对话循环
        while (!state.isTerminated()) {
            state.incrementRound();
            log.info("【AutoGen群聊】第 {} 轮，发言者: {}", state.getCurrentRound(), currentSpeaker.getName());

            // 让当前发言者接收并回复
            ConversableAgent sender = findAgentByName(state.getLastSpeaker());
            String reply = currentSpeaker.receive(currentMessage, sender != null ? sender : currentSpeaker, state);

            // 检查用户输入后的终止信号
            if ("TERMINATE".equalsIgnoreCase(reply.trim())) {
                log.info("【AutoGen群聊】收到用户终止信号");
                state.terminate("用户请求终止");
                break;
            }

            // 检查终止条件
            if (terminationCondition.shouldTerminate(state)) {
                String reason = terminationCondition.getTerminationReason(state);
                log.info("【AutoGen群聊】终止条件触发: {}", reason);
                state.terminate(reason);
                break;
            }

            // 准备下一轮
            currentMessage = reply;
            currentSpeaker = manager.selectNextSpeaker(agents, state);

            // 防止无限循环
            if (state.getCurrentRound() > 100) {
                log.warn("【AutoGen群聊】达到安全上限 100 轮，强制终止");
                state.terminate("达到安全上限");
                break;
            }
        }

        // 生成最终结果
        String finalPlan = generateFinalPlan();
        log.info("【AutoGen群聊】对话结束，共 {} 轮", state.getCurrentRound());

        return new GroupChatResult(state, finalPlan, state.getTerminationReason(), state.getCurrentRound());
    }

    /**
     * 重置对话状态
     */
    public void reset() {
        state.reset();
        for (ConversableAgent agent : agents) {
            agent.reset();
        }
    }

    private ConversableAgent findUserProxy() {
        return agents.stream()
            .filter(a -> a instanceof UserProxyAgent)
            .findFirst()
            .orElse(null);
    }

    private ConversableAgent findAgentByName(String name) {
        if (name == null) return null;
        return agents.stream()
            .filter(a -> a.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    private String generateFinalPlan() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== AutoGen 对话结果 ===\n\n");

        sb.append("## 对话历史\n");
        for (ConversationMessage msg : state.getMessages()) {
            if (msg.type() == MessageType.TEXT || msg.type() == MessageType.HUMAN_INPUT) {
                sb.append("**").append(msg.agentName()).append("**: ")
                    .append(msg.content()).append("\n\n");
            }
        }

        sb.append("## 终止原因\n");
        sb.append(state.getTerminationReason()).append("\n");

        return sb.toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<ConversableAgent> agents = new ArrayList<>();
        private GroupChatManager manager = GroupChatManager.builder().roundRobin().build();
        private TerminationCondition terminationCondition = TerminationCondition.builder().build();

        public Builder addAgent(ConversableAgent agent) {
            this.agents.add(agent);
            return this;
        }

        public Builder agents(List<ConversableAgent> agents) {
            this.agents.addAll(agents);
            return this;
        }

        public Builder manager(GroupChatManager manager) {
            this.manager = manager;
            return this;
        }

        public Builder terminationCondition(TerminationCondition condition) {
            this.terminationCondition = condition;
            return this;
        }

        public GroupChat build() {
            return new GroupChat(agents, manager, terminationCondition);
        }
    }
}
