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
 * 串行链式模式编排器
 * Agent按固定顺序执行：景点推荐 → 交通规划 → 住宿推荐
 * 前一个Agent的输出作为后一个Agent的输入
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SequentialChainOrchestrator {

    private final ChatModel chatModel;
    private final AttractionAgent attractionAgent;
    private final HotelAgent hotelAgent;
    private final TransportAgent transportAgent;

    /**
     * 串行执行规划
     * 按顺序：景点 → 交通 → 住宿 → 整合
     */
    public String planSequential(String destination, int days, String budget, String preferences) throws GraphRunnerException {
        log.info("【串行模式】开始执行规划: destination={}, days={}", destination, days);

        // Step 1: 景点推荐
        log.info("【串行模式】Step 1 - 景点推荐");
        ReactAgent attractionReactAgent = attractionAgent.createAgent();
        String attractionPrompt = buildAttractionPrompt(destination, days, preferences);
        String attractionResult = attractionReactAgent.call(attractionPrompt).toString();
        log.info("【串行模式】景点推荐完成，结果长度: {}", attractionResult.length());

        // Step 2: 交通规划（基于景点结果）
        log.info("【串行模式】Step 2 - 交通规划");
        ReactAgent transportReactAgent = transportAgent.createAgent();
        String transportPrompt = buildTransportPrompt(destination, attractionResult);
        String transportResult = transportReactAgent.call(transportPrompt).toString();
        log.info("【串行模式】交通规划完成，结果长度: {}", transportResult.length());

        // Step 3: 住宿推荐（基于景点和交通）
        log.info("【串行模式】Step 3 - 住宿推荐");
        ReactAgent hotelReactAgent = hotelAgent.createAgent();
        String hotelPrompt = buildHotelPrompt(destination, attractionResult, budget);
        String hotelResult = hotelReactAgent.call(hotelPrompt).toString();
        log.info("【串行模式】住宿推荐完成，结果长度: {}", hotelResult.length());

        // Step 4: 整合结果
        log.info("【串行模式】Step 4 - 整合结果");
        String finalPlan = aggregateResults(attractionResult, transportResult, hotelResult);
        log.info("【串行模式】规划完成，总结果长度: {}", finalPlan.length());

        return finalPlan;
    }

    /**
     * 构建景点推荐提示词
     */
    private String buildAttractionPrompt(String destination, int days, String preferences) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请为以下旅行需求推荐景点：\n");
        prompt.append("- 目的地：").append(destination).append("\n");
        prompt.append("- 游玩天数：").append(days).append("天\n");
        if (preferences != null && !preferences.isEmpty()) {
            prompt.append("- 偏好：").append(preferences).append("\n");
        }
        prompt.append("\n请按照天数的安排，推荐具体的景点和游览顺序。");
        return prompt.toString();
    }

    /**
     * 构建交通规划提示词（基于景点结果）
     */
    private String buildTransportPrompt(String destination, String attractionResult) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下景点安排规划交通：\n\n");
        prompt.append("目的地：").append(destination).append("\n\n");
        prompt.append("景点安排如下：\n").append(attractionResult).append("\n\n");
        prompt.append("请规划城际交通和市内交通，确保游览顺序合理。");
        return prompt.toString();
    }

    /**
     * 构建住宿推荐提示词（基于景点和交通）
     */
    private String buildHotelPrompt(String destination, String attractionResult, String budget) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下行程安排推荐住宿：\n\n");
        prompt.append("目的地：").append(destination).append("\n\n");
        prompt.append("景点安排：\n").append(attractionResult).append("\n\n");
        if (budget != null && !budget.isEmpty()) {
            prompt.append("预算范围：").append(budget).append("\n\n");
        }
        prompt.append("请推荐住宿区域和具体酒店，考虑交通便利性。");
        return prompt.toString();
    }

    /**
     * 整合各Agent的结果
     */
    private String aggregateResults(String attractionResult, String transportResult, String hotelResult) throws GraphRunnerException {
        // 使用LLM来整合结果
        ReactAgent aggregator = ReactAgent.builder()
            .name("aggregator_agent")
            .model(chatModel)
            .systemPrompt("""
                你是旅游规划整合专家。
                请将景点推荐、交通规划、住宿推荐三部分内容整合成一份完整的旅游规划方案。

                整合要求：
                1. 保持各部分的核心信息
                2. 确保内容之间的逻辑关联
                3. 添加费用估算和温馨提示
                4. 格式清晰、易于阅读
                """)
            .build();

        String aggregatePrompt = String.format("""
            请整合以下三部分内容，生成完整的旅游规划方案：

            ## 景点推荐
            %s

            ## 交通规划
            %s

            ## 住宿推荐
            %s

            请生成一份格式规范、内容完整的旅行方案。
            """, attractionResult, transportResult, hotelResult);

        return aggregator.call(aggregatePrompt).toString();
    }
}