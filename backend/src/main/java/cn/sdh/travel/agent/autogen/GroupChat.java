package cn.sdh.travel.agent.autogen;

import cn.sdh.travel.service.CheckpointService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
 * 6. 支持checkpoint持久化保存和恢复
 */
@Slf4j
public class GroupChat {

    @Getter
    private final List<ConversableAgent> agents = new ArrayList<>();

    private final GroupChatManager manager;
    private final TerminationCondition terminationCondition;
    private final ConversationState state;

    @Getter
    private final String sessionId;

    private final CheckpointService checkpointService;

    private final Consumer<ConversationState> onPauseCallback;

    public GroupChat(List<ConversableAgent> agents, GroupChatManager manager,
                      TerminationCondition terminationCondition, String sessionId,
                      CheckpointService checkpointService, Consumer<ConversationState> onPauseCallback) {
        this.agents.addAll(agents);
        this.manager = manager;
        this.terminationCondition = terminationCondition;
        this.state = new ConversationState();
        this.sessionId = sessionId;
        this.checkpointService = checkpointService;
        this.onPauseCallback = onPauseCallback;
    }

    /**
     * 执行群聊
     */
    public GroupChatResult run(String initialMessage) {
        log.info("【AutoGen群聊】开始执行，共 {} 个 Agent", agents.size());
        reset();

        // 为所有 Agent 设置暂停回调
        setupPauseCallback();

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

            // 每轮结束后保存checkpoint
            saveCheckpoint();
        }

        // 生成最终结果
        String finalPlan = generateFinalPlan();
        log.info("【AutoGen群聊】对话结束，共 {} 轮", state.getCurrentRound());

        // 保存最终状态
        if (checkpointService != null && sessionId != null) {
            checkpointService.completeSession(sessionId, state.getTerminationReason());
        }

        return new GroupChatResult(state, finalPlan, state.getTerminationReason(), state.getCurrentRound());
    }

    /**
     * 从已有状态恢复执行
     *
     * @param savedState 已保存的状态
     * @return 对话结果
     */
    public GroupChatResult resume(ConversationState savedState) {
        log.info("【AutoGen群聊】从checkpoint恢复执行");

        // 为所有 Agent 设置暂停回调
        setupPauseCallback();

        // 恢复状态
        restoreState(savedState);

        // 找到最后的消息作为当前消息
        String currentMessage = state.getLastMessageContent();
        if (currentMessage == null) {
            return run("继续对话");
        }

        // 找到下一个发言者
        ConversableAgent currentSpeaker = manager.selectNextSpeaker(agents, state);

        // 继续对话循环
        while (!state.isTerminated()) {
            state.incrementRound();
            log.info("【AutoGen群聊】第 {} 轮，发言者: {}", state.getCurrentRound(), currentSpeaker.getName());

            ConversableAgent sender = findAgentByName(state.getLastSpeaker());
            String reply = currentSpeaker.receive(currentMessage, sender != null ? sender : currentSpeaker, state);

            if ("TERMINATE".equalsIgnoreCase(reply.trim())) {
                log.info("【AutoGen群聊】收到用户终止信号");
                state.terminate("用户请求终止");
                break;
            }

            if (terminationCondition.shouldTerminate(state)) {
                String reason = terminationCondition.getTerminationReason(state);
                log.info("【AutoGen群聊】终止条件触发: {}", reason);
                state.terminate(reason);
                break;
            }

            currentMessage = reply;
            currentSpeaker = manager.selectNextSpeaker(agents, state);

            if (state.getCurrentRound() > 100) {
                log.warn("【AutoGen群聊】达到安全上限 100 轮，强制终止");
                state.terminate("达到安全上限");
                break;
            }

            saveCheckpoint();
        }

        String finalPlan = generateFinalPlan();
        if (checkpointService != null && sessionId != null) {
            checkpointService.completeSession(sessionId, state.getTerminationReason());
        }

        return new GroupChatResult(state, finalPlan, state.getTerminationReason(), state.getCurrentRound());
    }

    /**
     * 获取当前对话状态（用于暂停时保存）
     */
    public ConversationState getCurrentState() {
        return state;
    }

    /**
     * 请求暂停（在等待用户输入前调用）
     */
    public void requestPause() {
        saveCheckpoint();
        if (onPauseCallback != null) {
            onPauseCallback.accept(state);
        }
        if (checkpointService != null && sessionId != null) {
            checkpointService.pauseSession(sessionId);
        }
    }

    private void saveCheckpoint() {
        if (checkpointService != null && sessionId != null) {
            try {
                checkpointService.saveState(sessionId, state);
                log.debug("【AutoGen群聊】Checkpoint已保存: round={}", state.getCurrentRound());
            } catch (Exception e) {
                log.warn("【AutoGen群聊】保存Checkpoint失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 为所有 Agent 设置暂停回调
     * 当 Agent 需要人工输入时，自动触发暂停并保存状态
     */
    private void setupPauseCallback() {
        for (ConversableAgent agent : agents) {
            agent.setOnRequestHumanInputCallback(s -> requestPause());
        }
    }

    private void restoreState(ConversationState savedState) {
        state.getMessages().clear();
        state.getMessages().addAll(savedState.getMessages());
        state.setCurrentRound(savedState.getCurrentRound());
        state.setLastSpeaker(savedState.getLastSpeaker());
        state.setTerminated(savedState.isTerminated());
        state.setTerminationReason(savedState.getTerminationReason());
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
        private String sessionId;
        private CheckpointService checkpointService;
        private Consumer<ConversationState> onPauseCallback;

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

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder checkpointService(CheckpointService checkpointService) {
            this.checkpointService = checkpointService;
            return this;
        }

        public Builder onPauseCallback(Consumer<ConversationState> callback) {
            this.onPauseCallback = callback;
            return this;
        }

        public GroupChat build() {
            return new GroupChat(agents, manager, terminationCondition, sessionId, checkpointService, onPauseCallback);
        }
    }
}
