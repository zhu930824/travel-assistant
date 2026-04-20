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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 并行执行模式编排器
 * 多个Agent同时独立执行，最后由整合Agent合并结果
 *
 * 特点:
 * 1. 各Agent独立执行，互不依赖
 * 2. 并行执行提高效率
 * 3. 最后统一整合结果
 *
 * 适用场景:
 * - 各子任务之间无依赖关系
 * - 需要快速获取多个维度的结果
 * - 结果可以并行生成后合并
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ParallelExecutionOrchestrator {

    private final ChatModel chatModel;
    private final AttractionAgent attractionAgent;
    private final HotelAgent hotelAgent;
    private final TransportAgent transportAgent;

    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    /**
     * 并行执行规划
     * 同时调用: 景点推荐、交通规划、住宿推荐
     */
    public ParallelPlanResult planParallel(String destination, int days, String budget, String preferences) throws GraphRunnerException {
        log.info("【并行模式】开始执行并行规划: destination={}, days={}", destination, days);

        long startTime = System.currentTimeMillis();

        // 构建统一的输入提示
        String basePrompt = buildBasePrompt(destination, days, budget, preferences);

        // 并行启动三个Agent
        CompletableFuture<String> attractionFuture = CompletableFuture.supplyAsync(() -> {
            log.info("【并行模式】启动景点推荐Agent");
            ReactAgent agent = attractionAgent.createAgent();
            String prompt = "作为景点推荐专家，" + basePrompt;
            try {
                return agent.call(prompt).getText();
            } catch (GraphRunnerException e) {
                throw new RuntimeException(e);
            }
        }, executorService);

        CompletableFuture<String> transportFuture = CompletableFuture.supplyAsync(() -> {
            log.info("【并行模式】启动交通规划Agent");
            ReactAgent agent = transportAgent.createAgent();
            String prompt = "作为交通规划专家，" + basePrompt;
            try {
                return agent.call(prompt).getText();
            } catch (GraphRunnerException e) {
                throw new RuntimeException(e);
            }
        }, executorService);

        CompletableFuture<String> hotelFuture = CompletableFuture.supplyAsync(() -> {
            log.info("【并行模式】启动住宿推荐Agent");
            ReactAgent agent = hotelAgent.createAgent();
            String prompt = "作为住宿推荐专家，" + basePrompt;
            try {
                return agent.call(prompt).getText();
            } catch (GraphRunnerException e) {
                throw new RuntimeException(e);
            }
        }, executorService);

        // 等待所有任务完成
        CompletableFuture.allOf(attractionFuture, transportFuture, hotelFuture).join();

        String attractionResult = attractionFuture.join();
        String transportResult = transportFuture.join();
        String hotelResult = hotelFuture.join();

        log.info("【并行模式】所有Agent执行完成，耗时: {}ms", System.currentTimeMillis() - startTime);

        // 整合结果
        String finalPlan = aggregateResults(attractionResult, transportResult, hotelResult, destination);

        return new ParallelPlanResult(attractionResult, transportResult, hotelResult, finalPlan);
    }

    private String buildBasePrompt(String destination, int days, String budget, String preferences) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请为以下旅行需求提供建议：\n");
        prompt.append("- 目的地：").append(destination).append("\n");
        prompt.append("- 游玩天数：").append(days).append("天\n");
        if (budget != null && !budget.isEmpty()) {
            prompt.append("- 预算范围：").append(budget).append("\n");
        }
        if (preferences != null && !preferences.isEmpty()) {
            prompt.append("- 偏好：").append(preferences).append("\n");
        }
        return prompt.toString();
    }

    private String aggregateResults(String attractionResult, String transportResult,
                                    String hotelResult, String destination) throws GraphRunnerException {
        log.info("【并行模式】开始整合结果");

        ReactAgent aggregator = ReactAgent.builder()
            .name("parallel_aggregator_agent")
            .model(chatModel)
            .systemPrompt("""
                你是旅游规划整合专家。
                请将并行执行的三个专家的建议整合成一份协调一致的旅游规划方案。

                整合要求：
                1. 确保景点、交通、住宿三方面信息协调一致
                2. 指出任何可能的时间或地点冲突
                3. 提供优化建议
                4. 格式清晰，便于阅读
                """)
            .build();

        String aggregatePrompt = String.format("""
            请整合以下三位专家的建议，生成协调一致的旅游规划方案：

            ## 景点推荐专家建议
            %s

            ## 交通规划专家建议
            %s

            ## 住宿推荐专家建议
            %s

            请确保三方面建议的协调性，生成完整的旅行方案。
            """, attractionResult, transportResult, hotelResult);

        return aggregator.call(aggregatePrompt).getText();
    }

    /**
     * 并行规划结果
     */
    public record ParallelPlanResult(
        String attractionResult,
        String transportResult,
        String hotelResult,
        String finalPlan
    ) {}
}
