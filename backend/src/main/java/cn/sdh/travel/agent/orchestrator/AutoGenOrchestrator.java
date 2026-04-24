package cn.sdh.travel.agent.orchestrator;

import cn.sdh.travel.agent.autogen.*;
import cn.sdh.travel.agent.expert.AttractionAgent;
import cn.sdh.travel.agent.expert.HotelAgent;
import cn.sdh.travel.agent.expert.TransportAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * AutoGen 模式编排器
 * 实现支持人类参与的多 Agent 对话协作模式
 *
 * 核心特点:
 * 1. Human-in-the-loop - 用户可以在对话过程中提供反馈
 * 2. 多轮对话协作 - Agent 之间通过对话交流完善方案
 * 3. 灵活的终止控制 - 可配置终止条件和最大轮数
 * 4. 对话历史共享 - 所有 Agent 可以看到完整的对话历史
 *
 * 适用场景:
 * - 需要人工审核和调整的复杂规划
 * - 用户希望参与决策过程
 * - 需要逐步细化方案的迭代场景
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AutoGenOrchestrator {

    private final ChatModel chatModel;
    private final AttractionAgent attractionAgent;
    private final HotelAgent hotelAgent;
    private final TransportAgent transportAgent;

    /**
     * 执行 AutoGen 对话规划
     *
     * @param destination 目的地
     * @param days 天数
     * @param budget 预算
     * @param preferences 偏好
     * @param humanInputProvider 人类输入回调函数（可为 null，使用自动模式）
     * @return 规划结果
     */
    public AutoGenPlanResult planWithAutoGen(String destination, int days,
                                              String budget, String preferences,
                                              Function<String, String> humanInputProvider) {
        log.info("【AutoGen模式】开始执行: destination={}, days={}", destination, days);

        long startTime = System.currentTimeMillis();

        // 构建初始消息
        String initialMessage = buildInitialMessage(destination, days, budget, preferences);

        // 创建专家 Agent
        List<ConversableAgent> agents = createAgents(destination, days, budget, preferences, humanInputProvider);

        // 配置终止条件
        TerminationCondition terminationCondition = TerminationCondition.builder()
            .maxRounds(15)
            .addTerminationKeyword("TERMINATE")
            .addTerminationKeyword("方案已完成")
            .addTerminationKeyword("满意")
            .addTerminationKeyword("确认方案")
            .customCondition(this::checkCustomTermination)
            .build();

        // 创建群聊
        GroupChat groupChat = GroupChat.builder()
            .agents(agents)
            .manager(GroupChatManager.builder().smart().build())
            .terminationCondition(terminationCondition)
            .build();

        // 执行对话
        GroupChatResult result = groupChat.run(initialMessage);

        long duration = System.currentTimeMillis() - startTime;
        log.info("【AutoGen模式】执行完成，共 {} 轮对话，耗时 {}ms",
            result.totalRounds(), duration);

        return new AutoGenPlanResult(
            result.state(),
            result.finalPlan(),
            result.terminationReason(),
            result.totalRounds(),
            duration
        );
    }

    /**
     * 使用自动模式（无人工参与）执行规划
     */
    public AutoGenPlanResult planAuto(String destination, int days,
                                        String budget, String preferences) {
        return planWithAutoGen(destination, days, budget, preferences, null);
    }

    private String buildInitialMessage(String destination, int days, String budget, String preferences) {
        StringBuilder sb = new StringBuilder();
        sb.append("我需要一个旅游规划方案。\n\n");
        sb.append("需求信息:\n");
        sb.append("- 目的地: ").append(destination).append("\n");
        sb.append("- 游玩天数: ").append(days).append("天\n");
        if (budget != null && !budget.isEmpty()) {
            sb.append("- 预算: ").append(budget).append("\n");
        }
        if (preferences != null && !preferences.isEmpty()) {
            sb.append("- 偏好: ").append(preferences).append("\n");
        }
        sb.append("\n请各位专家提供专业建议，我们将通过讨论完善方案。");
        return sb.toString();
    }

    private List<ConversableAgent> createAgents(String destination, int days,
                                                  String budget, String preferences,
                                                  Function<String, String> humanInputProvider) {
        List<ConversableAgent> agents = new ArrayList<>();

        // 获取工具
        List<ToolCallback> attractionTools = getTools(attractionAgent);
        List<ToolCallback> hotelTools = getTools(hotelAgent);
        List<ToolCallback> transportTools = getTools(transportAgent);

        // 用户代理 Agent
        UserProxyAgent userProxy = UserProxyAgent.builder()
            .name("user_proxy")
            .description("用户代理，代表用户提供反馈")
            .chatModel(chatModel)
            .humanInputMode(humanInputProvider != null ? HumanInputMode.TERMINATE : HumanInputMode.NEVER)
            .humanInputProvider(humanInputProvider)
            .build();
        agents.add(userProxy);

        // 景点专家 Agent
        AssistantAgent attractionExpert = AssistantAgent.builder()
            .name("attraction_expert")
            .description("景点推荐专家")
            .chatModel(chatModel)
            .systemPrompt(buildAttractionPrompt(destination, days, preferences))
            .tools(attractionTools)
            .build();
        agents.add(attractionExpert);

        // 住宿专家 Agent
        AssistantAgent hotelExpert = AssistantAgent.builder()
            .name("hotel_expert")
            .description("住宿推荐专家")
            .chatModel(chatModel)
            .systemPrompt(buildHotelPrompt(destination, days, budget))
            .tools(hotelTools)
            .build();
        agents.add(hotelExpert);

        // 交通专家 Agent
        AssistantAgent transportExpert = AssistantAgent.builder()
            .name("transport_expert")
            .description("交通规划专家")
            .chatModel(chatModel)
            .systemPrompt(buildTransportPrompt(destination, days))
            .tools(transportTools)
            .build();
        agents.add(transportExpert);

        // 协调者 Agent（负责整合各方意见）
        AssistantAgent coordinator = AssistantAgent.builder()
            .name("coordinator")
            .description("方案协调者，整合各专家意见")
            .chatModel(chatModel)
            .systemPrompt(buildCoordinatorPrompt())
            .tools(List.of())
            .build();
        agents.add(coordinator);

        return agents;
    }

    private List<ToolCallback> getTools(Object agentWithTools) {
        try {
            return List.of(MethodToolCallbackProvider.builder()
                .toolObjects(agentWithTools)
                .build()
                .getToolCallbacks());
        } catch (Exception e) {
            log.warn("获取工具失败: {}", e.getMessage());
            return List.of();
        }
    }

    private String buildAttractionPrompt(String destination, int days, String preferences) {
        return """
            你是景点推荐专家。

            职责:
            - 根据目的地和天数推荐合适的景点
            - 考虑用户的偏好安排游览顺序
            - 提供景点开放时间、门票价格等信息

            目的地: %s
            天数: %d天
            偏好: %s

            在对话中，请根据其他专家的发言，补充或调整景点建议。
            使用可用工具获取真实景点数据。
            """.formatted(destination, days, preferences != null ? preferences : "无特别偏好");
    }

    private String buildHotelPrompt(String destination, int days, String budget) {
        return """
            你是住宿推荐专家。

            职责:
            - 根据目的地和预算推荐合适的住宿
            - 考虑景点位置选择住宿区域
            - 提供酒店设施、价格等信息

            目的地: %s
            天数: %d天
            预算: %s

            在对话中，请根据景点专家的建议，调整住宿推荐。
            使用可用工具获取真实酒店数据。
            """.formatted(destination, days, budget != null ? budget : "未指定");
    }

    private String buildTransportPrompt(String destination, int days) {
        return """
            你是交通规划专家。

            职责:
            - 规划城际交通方案
            - 安排市内交通路线
            - 提供交通时间和费用估算

            目的地: %s
            天数: %d天

            在对话中，请根据景点和住宿的安排，优化交通路线。
            使用可用工具获取真实交通数据。
            """.formatted(destination, days);
    }

    private String buildCoordinatorPrompt() {
        return """
            你是方案协调者。

            职责:
            - 整合各专家的建议
            - 发现并指出方案中的矛盾或不足
            - 总结讨论成果，形成完整方案

            在每次发言时:
            1. 总结之前的讨论要点
            2. 指出需要进一步讨论的问题
            3. 推动对话向最终方案方向发展

            如果各方意见一致且方案完整，请输出"方案已完成"。
            """;
    }

    private boolean checkCustomTermination(ConversationState state) {
        ConversationMessage lastMsg = state.getLastMessage();
        if (lastMsg == null) return false;

        if (lastMsg.agentName().equals("coordinator")) {
            // 检查协调者是否认为方案完成
            return lastMsg.content().contains("方案已完成")
                || lastMsg.content().contains("综合规划方案");
        }
        return false;
    }

    /**
     * AutoGen 规划结果
     */
    public record AutoGenPlanResult(
        ConversationState conversationState,
        String finalPlan,
        String terminationReason,
        int totalRounds,
        long durationMs
    ) {
        public String getConversationHistory() {
            if (conversationState == null) return "";
            return conversationState.formatHistory();
        }

        public int getMessageCount() {
            if (conversationState == null) return 0;
            return conversationState.getMessages().size();
        }
    }
}
