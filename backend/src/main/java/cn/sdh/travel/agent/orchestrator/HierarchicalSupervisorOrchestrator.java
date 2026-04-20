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

/**
 * 层级监督模式编排器
 * 监督者Agent负责分配任务和审核结果，专家Agent执行具体任务
 *
 * 特点:
 * 1. 监督者Agent作为中央控制器
 * 2. 监督者分解任务、分配给专家、审核结果
 * 3. 支持迭代优化，不满意可重新分配
 *
 * 适用场景:
 * - 复杂任务需要分解
 * - 需要质量把控
 * - 任务之间有依赖关系
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HierarchicalSupervisorOrchestrator {

    private final ChatModel chatModel;
    private final AttractionAgent attractionAgent;
    private final HotelAgent hotelAgent;
    private final TransportAgent transportAgent;

    private static final int MAX_ITERATIONS = 3;

    /**
     * 层级监督执行
     */
    public SupervisedPlanResult planWithSupervisor(String destination, int days, String budget, String preferences) throws GraphRunnerException {
        log.info("【层级监督模式】开始执行: destination={}", destination);

        // Step 1: 监督者分析任务
        List<TaskAssignment> assignments = analyzeAndAssign(destination, days, budget, preferences);
        log.info("【层级监督模式】任务分配完成，共{}个任务", assignments.size());

        // Step 2: 执行任务
        List<TaskResult> taskResults = new ArrayList<>();
        for (TaskAssignment assignment : assignments) {
            log.info("【层级监督模式】执行任务: {} -> {}", assignment.taskId(), assignment.agentType());
            String result = executeAssignment(assignment);
            taskResults.add(new TaskResult(assignment.taskId(), assignment.agentType(), result));
        }

        // Step 3: 监督者审核结果
        SupervisionDecision decision = reviewResults(taskResults, destination, days, budget, preferences);

        int iteration = 0;
        while (!decision.approved() && iteration < MAX_ITERATIONS) {
            iteration++;
            log.info("【层级监督模式】第{}次迭代优化", iteration);

            // 根据反馈重新执行
            for (String taskId : decision.needsRevision()) {
                TaskAssignment assignment = assignments.stream()
                    .filter(a -> a.taskId().equals(taskId))
                    .findFirst()
                    .orElse(null);

                if (assignment != null) {
                    log.info("【层级监督模式】重新执行: {}", taskId);
                    String revisedPrompt = assignment.assignment() + "\n\n请注意以下反馈：" + decision.feedback();
                    String result = executeAssignment(new TaskAssignment(
                        assignment.taskId(),
                        assignment.agentType(),
                        revisedPrompt
                    ));

                    // 更新结果
                    taskResults.stream()
                        .filter(r -> r.taskId().equals(taskId))
                        .forEach(r -> {
                            int idx = taskResults.indexOf(r);
                            taskResults.set(idx, new TaskResult(taskId, assignment.agentType(), result));
                        });
                }
            }

            // 重新审核
            decision = reviewResults(taskResults, destination, days, budget, preferences);
        }

        // Step 4: 整合最终结果
        String finalPlan = generateFinalPlan(taskResults);
        log.info("【层级监督模式】规划完成，迭代次数: {}", iteration);

        return new SupervisedPlanResult(assignments, taskResults, finalPlan, decision.approved());
    }

    /**
     * 监督者分析和分配任务
     */
    private List<TaskAssignment> analyzeAndAssign(String destination, int days, String budget, String preferences) throws GraphRunnerException {
        ReactAgent supervisor = ReactAgent.builder()
            .name("supervisor_agent")
            .model(chatModel)
            .systemPrompt("""
                你是旅游规划监督者。负责:
                1. 分析用户需求
                2. 制定任务计划
                3. 分配任务给专家

                可用专家:
                - attraction: 景点推荐专家
                - hotel: 住宿推荐专家
                - transport: 交通规划专家

                按以下格式输出任务分配(每个任务一行):
                TASK_ID|AGENT_TYPE|ASSIGNMENT
                例如:
                task_1|attraction|推荐北京3天的主要景点
                task_2|hotel|推荐北京3天的住宿区域
                task_3|transport|规划北京3天的交通路线
                """)
            .build();

        String analyzePrompt = String.format("""
            分析以下旅游需求，分配任务给各专家：

            目的地：%s
            天数：%d天
            预算：%s
            偏好：%s

            输出任务分配清单。
            """, destination, days,
                budget != null ? budget : "未指定",
                preferences != null ? preferences : "无特别偏好");

        String response = supervisor.call(analyzePrompt).getText();

        // 解析任务分配
        return parseAssignments(response);
    }

    private List<TaskAssignment> parseAssignments(String response) {
        List<TaskAssignment> assignments = new ArrayList<>();

        for (String line : response.split("\n")) {
            line = line.trim();
            if (line.contains("|") && !line.startsWith("#")) {
                String[] parts = line.split("\\|", 3);
                if (parts.length == 3) {
                    String taskId = parts[0].trim();
                    String agentType = parts[1].trim();
                    String assignment = parts[2].trim();

                    try {
                        AgentType type = AgentType.valueOf(agentType.toUpperCase());
                        assignments.add(new TaskAssignment(taskId, type, assignment));
                    } catch (IllegalArgumentException e) {
                        log.warn("未知的Agent类型: {}", agentType);
                    }
                }
            }
        }

        // 默认分配
        if (assignments.isEmpty()) {
            assignments.add(new TaskAssignment("task_1", AgentType.ATTRACTION, "推荐景点"));
            assignments.add(new TaskAssignment("task_2", AgentType.HOTEL, "推荐住宿"));
            assignments.add(new TaskAssignment("task_3", AgentType.TRANSPORT, "规划交通"));
        }

        return assignments;
    }

    /**
     * 执行任务分配
     */
    private String executeAssignment(TaskAssignment assignment) throws GraphRunnerException {
        ReactAgent agent = switch (assignment.agentType()) {
            case ATTRACTION -> attractionAgent.createAgent();
            case HOTEL -> hotelAgent.createAgent();
            case TRANSPORT -> transportAgent.createAgent();
        };

        return agent.call(assignment.assignment()).getText();
    }

    /**
     * 监督者审核结果
     */
    private SupervisionDecision reviewResults(List<TaskResult> results, String destination,
                                               int days, String budget, String preferences) throws GraphRunnerException {
        ReactAgent reviewer = ReactAgent.builder()
            .name("supervisor_reviewer")
            .model(chatModel)
            .systemPrompt("""
                你是旅游规划审核专家。审核各专家的结果，判断是否满足用户需求。

                输出格式:
                APPROVED: true/false
                FEEDBACK: 具体反馈意见
                NEEDS_REVISION: 需要修改的任务ID列表(逗号分隔)

                如果所有结果都满意，输出 APPROVED: true
                如果需要修改，输出需要修改的任务ID。
                """)
            .build();

        StringBuilder reviewPrompt = new StringBuilder();
        reviewPrompt.append("用户需求:\n");
        reviewPrompt.append("- 目的地: ").append(destination).append("\n");
        reviewPrompt.append("- 天数: ").append(days).append("天\n");
        reviewPrompt.append("- 预算: ").append(budget != null ? budget : "未指定").append("\n\n");

        reviewPrompt.append("专家结果:\n");
        for (TaskResult result : results) {
            reviewPrompt.append("---\n");
            reviewPrompt.append("任务: ").append(result.taskId()).append("\n");
            reviewPrompt.append("结果:\n").append(result.result()).append("\n");
        }

        String response = reviewer.call(reviewPrompt.toString()).getText();
        return parseSupervisionDecision(response);
    }

    private SupervisionDecision parseSupervisionDecision(String response) {
        boolean approved = response.contains("APPROVED: true") ||
                           response.contains("APPROVED:true");

        String feedback = "";
        int feedbackIdx = response.indexOf("FEEDBACK:");
        if (feedbackIdx >= 0) {
            int endIdx = response.indexOf("\n", feedbackIdx);
            feedback = endIdx > 0 ?
                response.substring(feedbackIdx + 9, endIdx).trim() :
                response.substring(feedbackIdx + 9).trim();
        }

        List<String> needsRevision = new ArrayList<>();
        int revisionIdx = response.indexOf("NEEDS_REVISION:");
        if (revisionIdx >= 0) {
            int endIdx = response.indexOf("\n", revisionIdx);
            String revisionStr = endIdx > 0 ?
                response.substring(revisionIdx + 15, endIdx).trim() :
                response.substring(revisionIdx + 15).trim();
            for (String id : revisionStr.split(",")) {
                if (!id.trim().isEmpty()) {
                    needsRevision.add(id.trim());
                }
            }
        }

        return new SupervisionDecision(approved, feedback, needsRevision);
    }

    private String generateFinalPlan(List<TaskResult> results) throws GraphRunnerException {
        ReactAgent finalizer = ReactAgent.builder()
            .name("final_aggregator")
            .model(chatModel)
            .systemPrompt("你是旅游方案整合专家，将各专家的建议整合成完整的旅游规划。")
            .build();

        StringBuilder prompt = new StringBuilder("请整合以下专家建议: \n");
        for (TaskResult result : results) {
            prompt.append("\n## ").append(result.agentType()).append("\n");
            prompt.append(result.result()).append("\n");
        }

        return finalizer.call(prompt.toString()).getText();
    }

    // Types
    public enum AgentType {
        ATTRACTION, HOTEL, TRANSPORT
    }

    public record TaskAssignment(String taskId, AgentType agentType, String assignment) {}
    public record TaskResult(String taskId, AgentType agentType, String result) {}
    public record SupervisionDecision(boolean approved, String feedback, List<String> needsRevision) {}
    public record SupervisedPlanResult(
        List<TaskAssignment> assignments,
        List<TaskResult> taskResults,
        String finalPlan,
        boolean approved
    ) {}
}
