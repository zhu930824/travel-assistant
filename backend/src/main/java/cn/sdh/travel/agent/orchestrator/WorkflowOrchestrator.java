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

import java.util.HashMap;
import java.util.Map;

/**
 * 工作流模式编排器
 * 预定义工作流步骤，支持条件分支和循环
 *
 * 特点:
 * 1. 定义固定的工作流步骤
 * 2. 支持条件判断和分支
 * 3. 支持步骤间的数据传递
 *
 * 适用场景:
 * - 流程固定的业务场景
 * - 需要条件判断的复杂流程
 * - 需要精细化控制执行过程
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowOrchestrator {

    private final ChatModel chatModel;
    private final AttractionAgent attractionAgent;
    private final HotelAgent hotelAgent;
    private final TransportAgent transportAgent;

    /**
     * 工作流步骤定义
     */
    public enum WorkflowStep {
        ANALYZE("分析需求"),
        ATTRACTION("景点推荐"),
        HOTEL("住宿推荐"),
        TRANSPORT("交通规划"),
        VALIDATE("验证方案"),
        OPTIMIZE("优化方案"),
        FINALIZE("生成最终方案");

        private final String description;

        WorkflowStep(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 执行工作流
     */
    public WorkflowPlanResult executeWorkflow(String destination, int days, String budget, String preferences) throws GraphRunnerException {
        log.info("【工作流模式】开始执行工作流");

        WorkflowContext context = new WorkflowContext(destination, days, budget, preferences);

        // Step 1: 分析需求
        executeStep(context, WorkflowStep.ANALYZE, this::analyzeNeeds);

        // Step 2: 景点推荐
        executeStep(context, WorkflowStep.ATTRACTION, this::recommendAttractions);

        // Step 3: 根据预算条件决定是否推荐高端住宿
        if (shouldRecommendPremiumHotel(context)) {
            executeStep(context, WorkflowStep.HOTEL, this::recommendPremiumHotel);
        } else {
            executeStep(context, WorkflowStep.HOTEL, this::recommendStandardHotel);
        }

        // Step 4: 交通规划
        executeStep(context, WorkflowStep.TRANSPORT, this::planTransport);

        // Step 5: 验证方案
        boolean valid = executeStep(context, WorkflowStep.VALIDATE, this::validatePlan);

        // Step 6: 如果验证不通过，优化方案
        if (!valid) {
            executeStep(context, WorkflowStep.OPTIMIZE, this::optimizePlan);
        }

        // Step 7: 生成最终方案
        executeStep(context, WorkflowStep.FINALIZE, this::finalizePlan);

        return new WorkflowPlanResult(context);
    }

    /**
     * 执行工作流步骤
     */
    private <T> T executeStep(WorkflowContext context, WorkflowStep step, StepExecutor<T> executor) throws GraphRunnerException {
        log.info("【工作流模式】执行步骤: {} - {}", step.name(), step.getDescription());
        long startTime = System.currentTimeMillis();

        T result = executor.execute(context);

        long duration = System.currentTimeMillis() - startTime;
        context.addStepResult(step.name(), result);
        context.addStepDuration(step.name(), duration);

        log.info("【工作流模式】步骤完成: {}, 耗时: {}ms", step.name(), duration);
        return result;
    }

    @FunctionalInterface
    private interface StepExecutor<T> {
        T execute(WorkflowContext context) throws GraphRunnerException;
    }

    // ===== 步骤实现 =====

    private Void analyzeNeeds(WorkflowContext context) throws GraphRunnerException {
        ReactAgent analyzer = ReactAgent.builder()
            .name("need_analyzer")
            .model(chatModel)
            .systemPrompt("你是旅游需求分析专家，分析用户需求并提取关键信息。")
            .build();

        String prompt = String.format("""
            分析以下旅游需求：
            目的地：%s
            天数：%d天
            预算：%s
            偏好：%s

            请输出需求分析结果，包括：
            1. 核心需求
            2. 预算等级(高/中/低)
            3. 推荐行程节奏
            """, context.getDestination(), context.getDays(),
                context.getBudget() != null ? context.getBudget() : "未指定",
                context.getPreferences() != null ? context.getPreferences() : "无特别偏好");

        String analysis = analyzer.call(prompt).getText();
        context.setAnalysisResult(analysis);
        return null;
    }

    private Void recommendAttractions(WorkflowContext context) throws GraphRunnerException {
        ReactAgent agent = attractionAgent.createAgent();
        String prompt = String.format("""
            推荐景点：
            目的地：%s
            天数：%d天
            偏好：%s
            """, context.getDestination(), context.getDays(),
                context.getPreferences() != null ? context.getPreferences() : "无");

        String result = agent.call(prompt).getText();
        context.setAttractionResult(result);
        return null;
    }

    private boolean shouldRecommendPremiumHotel(WorkflowContext context) {
        String budget = context.getBudget();
        if (budget == null) return false;
        return budget.contains("高") || budget.contains("豪华") || budget.contains("高端");
    }

    private Void recommendPremiumHotel(WorkflowContext context) throws GraphRunnerException {
        ReactAgent agent = hotelAgent.createAgent();
        String prompt = String.format("""
            推荐高端住宿：
            目的地：%s
            天数：%d天
            景点安排：%s
            """, context.getDestination(), context.getDays(),
                context.getAttractionResult());

        String result = agent.call(prompt + "\n请推荐高端/豪华酒店。").getText();
        context.setHotelResult(result);
        return null;
    }

    private Void recommendStandardHotel(WorkflowContext context) throws GraphRunnerException {
        ReactAgent agent = hotelAgent.createAgent();
        String prompt = String.format("""
            推荐住宿：
            目的地：%s
            天数：%d天
            预算：%s
            景点安排：%s
            """, context.getDestination(), context.getDays(),
                context.getBudget() != null ? context.getBudget() : "经济",
                context.getAttractionResult());

        String result = agent.call(prompt).getText();
        context.setHotelResult(result);
        return null;
    }

    private Void planTransport(WorkflowContext context) throws GraphRunnerException {
        ReactAgent agent = transportAgent.createAgent();
        String prompt = String.format("""
            规划交通：
            目的地：%s
            天数：%d天
            景点安排：%s
            住宿安排：%s
            """, context.getDestination(), context.getDays(),
                context.getAttractionResult(),
                context.getHotelResult());

        String result = agent.call(prompt).getText();
        context.setTransportResult(result);
        return null;
    }

    private boolean validatePlan(WorkflowContext context) throws GraphRunnerException {
        ReactAgent validator = ReactAgent.builder()
            .name("plan_validator")
            .model(chatModel)
            .systemPrompt("""
                你是旅游方案验证专家。验证方案的合理性和可行性。

                输出格式：
                VALID: true/false
                ISSUES: 发现的问题列表
                """)
            .build();

        String prompt = String.format("""
            验证以下旅游方案：

            ## 景点安排
            %s

            ## 住宿安排
            %s

            ## 交通安排
            %s

            请验证方案的合理性。
            """, context.getAttractionResult(),
                context.getHotelResult(),
                context.getTransportResult());

        String response = validator.call(prompt).getText();
        boolean valid = response.contains("VALID: true") || response.contains("VALID:true");
        context.setValidationResult(response);
        return valid;
    }

    private Void optimizePlan(WorkflowContext context) throws GraphRunnerException {
        ReactAgent optimizer = ReactAgent.builder()
            .name("plan_optimizer")
            .model(chatModel)
            .systemPrompt("你是旅游方案优化专家，根据验证结果优化方案。")
            .build();

        String prompt = String.format("""
            根据以下验证结果优化方案：

            ## 原始方案
            景点：%s
            住宿：%s
            交通：%s

            ## 验证结果
            %s

            请输出优化后的方案。
            """, context.getAttractionResult(),
                context.getHotelResult(),
                context.getTransportResult(),
                context.getValidationResult());

        String optimizedPlan = optimizer.call(prompt).getText();
        context.setOptimizedResult(optimizedPlan);
        return null;
    }

    private Void finalizePlan(WorkflowContext context) throws GraphRunnerException {
        ReactAgent finalizer = ReactAgent.builder()
            .name("plan_finalizer")
            .model(chatModel)
            .systemPrompt("""
                你是旅游方案整合专家。
                将景点、住宿、交通整合成完整的旅游规划方案。
                输出格式清晰、便于阅读的最终方案。
                """)
            .build();

        String baseContent = context.getOptimizedResult() != null ?
            context.getOptimizedResult() :
            String.format("""
                景点：%s
                住宿：%s
                交通：%s
                """, context.getAttractionResult(),
                    context.getHotelResult(),
                    context.getTransportResult());

        String prompt = String.format("""
            整合以下内容生成最终旅游方案：

            %s

            请生成完整的旅行计划。
            """, baseContent);

        String finalPlan = finalizer.call(prompt).getText();
        context.setFinalPlan(finalPlan);
        return null;
    }

    // ===== Context =====

    public static class WorkflowContext {
        private final String destination;
        private final int days;
        private final String budget;
        private final String preferences;

        private String analysisResult;
        private String attractionResult;
        private String hotelResult;
        private String transportResult;
        private String validationResult;
        private String optimizedResult;
        private String finalPlan;

        private final Map<String, Object> stepResults = new HashMap<>();
        private final Map<String, Long> stepDurations = new HashMap<>();

        public WorkflowContext(String destination, int days, String budget, String preferences) {
            this.destination = destination;
            this.days = days;
            this.budget = budget;
            this.preferences = preferences;
        }

        // Getters
        public String getDestination() { return destination; }
        public int getDays() { return days; }
        public String getBudget() { return budget; }
        public String getPreferences() { return preferences; }
        public String getAnalysisResult() { return analysisResult; }
        public String getAttractionResult() { return attractionResult; }
        public String getHotelResult() { return hotelResult; }
        public String getTransportResult() { return transportResult; }
        public String getValidationResult() { return validationResult; }
        public String getOptimizedResult() { return optimizedResult; }
        public String getFinalPlan() { return finalPlan; }
        public Map<String, Object> getStepResults() { return stepResults; }
        public Map<String, Long> getStepDurations() { return stepDurations; }

        // Setters
        public void setAnalysisResult(String analysisResult) { this.analysisResult = analysisResult; }
        public void setAttractionResult(String attractionResult) { this.attractionResult = attractionResult; }
        public void setHotelResult(String hotelResult) { this.hotelResult = hotelResult; }
        public void setTransportResult(String transportResult) { this.transportResult = transportResult; }
        public void setValidationResult(String validationResult) { this.validationResult = validationResult; }
        public void setOptimizedResult(String optimizedResult) { this.optimizedResult = optimizedResult; }
        public void setFinalPlan(String finalPlan) { this.finalPlan = finalPlan; }

        public void addStepResult(String step, Object result) {
            stepResults.put(step, result);
        }

        public void addStepDuration(String step, long duration) {
            stepDurations.put(step, duration);
        }
    }

    // ===== Result =====

    public record WorkflowPlanResult(WorkflowContext context) {
        public String getFinalPlan() {
            return context.getFinalPlan();
        }

        public Map<String, Long> getStepDurations() {
            return context.getStepDurations();
        }
    }
}
