package cn.sdh.travel.agent.orchestrator;

import cn.sdh.travel.agent.expert.AttractionAgent;
import cn.sdh.travel.agent.expert.HotelAgent;
import cn.sdh.travel.agent.expert.TransportAgent;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

/**
 * 路由分发模式编排器
 * 根据请求类型路由到相应专家Agent处理
 *
 * 特点:
 * 1. 单一入口，智能路由
 * 2. 根据用户意图选择最合适的Agent
 * 3. 支持动态Agent选择
 *
 * 适用场景:
 * - 用户请求有多种类型
 * - 不同类型需要不同专业Agent处理
 * - 请求类型可以明确区分
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RouterDispatchOrchestrator {

    private final ChatModel chatModel;
    private final AttractionAgent attractionAgent;
    private final HotelAgent hotelAgent;
    private final TransportAgent transportAgent;

    /**
     * Agent类型枚举
     */
    public enum AgentType {
        ATTRACTION("景点推荐"),
        HOTEL("住宿推荐"),
        TRANSPORT("交通规划"),
        COMPREHENSIVE("综合规划");

        private final String description;

        AgentType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 路由执行
     * 先分析请求类型，再路由到对应Agent
     */
    public RoutedPlanResult planWithRoute(String destination, int days, String budget, String preferences) throws GraphRunnerException {
        log.info("【路由模式】开始分析请求类型: destination={}", destination);

        // Step 1: 路由分析
        AgentType agentType = analyzeRequestType(destination, days, budget, preferences);
        log.info("【路由模式】路由结果: {} - {}", agentType, agentType.getDescription());

        // Step 2: 根据路由结果执行对应Agent
        String result = executeAgent(agentType, destination, days, budget, preferences);

        return new RoutedPlanResult(agentType, result);
    }

    /**
     * 分析请求类型
     */
    private AgentType analyzeRequestType(String destination, int days, String budget, String preferences) throws GraphRunnerException {
        ReactAgent routerAgent = ReactAgent.builder()
            .name("router_agent")
            .model(chatModel)
            .systemPrompt("""
                你是一个请求分类器。根据用户的旅游需求，判断应该由哪个专家处理。

                类型说明:
                - ATTRACTION: 用户主要关心景点推荐
                - HOTEL: 用户主要关心住宿安排
                - TRANSPORT: 用户主要关心交通出行
                - COMPREHENSIVE: 需要全面规划（包含景点、住宿、交通）

                分析用户需求中的关键词和意图，输出最合适的类型。
                只输出类型名称，不要其他解释。
                """)
            .build();

        String analyzePrompt = String.format("""
            请分析以下旅游需求应该由哪类专家处理：

            目的地：%s
            天数：%d天
            预算：%s
            偏好：%s

            只输出类型名称(ATTRACTION/HOTEL/TRANSPORT/COMPREHENSIVE)。
            """, destination, days,
                budget != null ? budget : "未指定",
                preferences != null ? preferences : "无特别偏好");

        String response = routerAgent.call(analyzePrompt).getText().trim().toUpperCase();

        try {
            return AgentType.valueOf(response);
        } catch (IllegalArgumentException e) {
            log.warn("【路由模式】无法解析路由结果: {}, 默认使用综合规划", response);
            return AgentType.COMPREHENSIVE;
        }
    }

    /**
     * 执行对应Agent
     */
    private String executeAgent(AgentType agentType, String destination, int days,
                                String budget, String preferences) throws GraphRunnerException {
        log.info("【路由模式】执行Agent: {}", agentType);

        return switch (agentType) {
            case ATTRACTION -> {
                ReactAgent agent = attractionAgent.createAgent();
                String prompt = buildAttractionPrompt(destination, days, preferences);
                yield agent.call(prompt).getText();
            }
            case HOTEL -> {
                ReactAgent agent = hotelAgent.createAgent();
                String prompt = buildHotelPrompt(destination, days, budget);
                yield agent.call(prompt).getText();
            }
            case TRANSPORT -> {
                ReactAgent agent = transportAgent.createAgent();
                String prompt = buildTransportPrompt(destination, days);
                yield agent.call(prompt).getText();
            }
            case COMPREHENSIVE -> executeComprehensive(destination, days, budget, preferences);
        };
    }

    /**
     * 综合规划 - 调用所有专家后整合
     */
    private String executeComprehensive(String destination, int days, String budget, String preferences) throws GraphRunnerException {
        log.info("【路由模式】综合规划 - 调用所有专家");

        // 景点推荐
        ReactAgent attractionReactAgent = attractionAgent.createAgent();
        String attractionResult = attractionReactAgent.call(
            buildAttractionPrompt(destination, days, preferences)).getText();

        // 住宿推荐
        ReactAgent hotelReactAgent = hotelAgent.createAgent();
        String hotelResult = hotelReactAgent.call(
            buildHotelPrompt(destination, days, budget)).getText();

        // 交通规划
        ReactAgent transportReactAgent = transportAgent.createAgent();
        String transportResult = transportReactAgent.call(
            buildTransportPrompt(destination, days)).getText();

        // 整合结果
        return aggregateResults(attractionResult, hotelResult, transportResult);
    }

    private String buildAttractionPrompt(String destination, int days, String preferences) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请推荐").append(destination).append("的旅游景点：\n");
        prompt.append("- 游玩天数：").append(days).append("天\n");
        if (preferences != null && !preferences.isEmpty()) {
            prompt.append("- 偏好：").append(preferences).append("\n");
        }
        prompt.append("\n请提供详细的景点推荐和游览安排。");
        return prompt.toString();
    }

    private String buildHotelPrompt(String destination, int days, String budget) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请推荐").append(destination).append("的住宿：\n");
        prompt.append("- 游玩天数：").append(days).append("天\n");
        if (budget != null && !budget.isEmpty()) {
            prompt.append("- 预算范围：").append(budget).append("\n");
        }
        prompt.append("\n请推荐合适的住宿区域和酒店。");
        return prompt.toString();
    }

    private String buildTransportPrompt(String destination, int days) {
        return String.format("""
            请规划前往%s的交通方案：
            - 游玩天数：%d天

            请提供城际交通和市内交通建议。
            """, destination, days);
    }

    private String aggregateResults(String attractionResult, String hotelResult, String transportResult) throws GraphRunnerException {
        ReactAgent aggregator = ReactAgent.builder()
            .name("router_aggregator_agent")
            .model(chatModel)
            .systemPrompt("""
                你是旅游规划整合专家。
                请将景点推荐、住宿推荐、交通规划整合成完整的旅游方案。
                """)
            .build();

        String aggregatePrompt = String.format("""
            整合以下信息生成完整方案：

            ## 景点推荐
            %s

            ## 住宿推荐
            %s

            ## 交通规划
            %s
            """, attractionResult, hotelResult, transportResult);

        return aggregator.call(aggregatePrompt).getText();
    }

    /**
     * 路由规划结果
     */
    public record RoutedPlanResult(
        AgentType selectedAgent,
        String result
    ) {}
}
