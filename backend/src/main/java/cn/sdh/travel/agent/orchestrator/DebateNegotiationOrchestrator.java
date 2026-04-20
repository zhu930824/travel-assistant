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
 * 辩论协商模式编排器
 * 多个Agent通过辩论讨论达成最优方案
 *
 * 特点:
 * 1. 各Agent独立提出方案
 * 2. Agent之间相互评论和质疑
 * 3. 多轮辩论后达成共识
 *
 * 适用场景:
 * - 需要综合考虑多种观点
 * - 问题没有单一明确答案
 * - 需要权衡多方面因素
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DebateNegotiationOrchestrator {

    private final ChatModel chatModel;
    private final AttractionAgent attractionAgent;
    private final HotelAgent hotelAgent;
    private final TransportAgent transportAgent;

    private static final int MAX_ROUNDS = 3;

    /**
     * 辩论协商执行
     */
    public DebatePlanResult planWithDebate(String destination, int days, String budget, String preferences) throws GraphRunnerException {
        log.info("【辩论模式】开始执行: destination={}", destination);

        // Step 1: 各Agent提出初始方案
        List<AgentProposal> initialProposals = generateInitialProposals(destination, days, budget, preferences);
        log.info("【辩论模式】初始方案生成完成，共{}个方案", initialProposals.size());

        List<DebateRound> debateRounds = new ArrayList<>();

        // Step 2: 多轮辩论
        for (int round = 1; round <= MAX_ROUNDS; round++) {
            log.info("【辩论模式】第{}轮辩论开始", round);

            // 各Agent发表评论
            List<AgentComment> comments = new ArrayList<>();
            for (AgentProposal proposal : initialProposals) {
                comments.addAll(collectComments(proposal, initialProposals, round));
            }

            // 根据评论修正方案
            List<AgentProposal> revisedProposals = reviseProposals(initialProposals, comments, round);

            DebateRound debateRound = new DebateRound(round, comments, revisedProposals);
            debateRounds.add(debateRound);

            // 检查是否达成共识
            if (checkConsensus(revisedProposals)) {
                log.info("【辩论模式】第{}轮达成共识", round);
                break;
            }

            initialProposals = revisedProposals;
        }

        // Step 3: 最终共识整合
        String finalPlan = generateConsensusPlan(initialProposals);

        return new DebatePlanResult(debateRounds, finalPlan);
    }

    /**
     * 各Agent提出初始方案
     */
    private List<AgentProposal> generateInitialProposals(String destination, int days,
                                                          String budget, String preferences) throws GraphRunnerException {
        List<AgentProposal> proposals = new ArrayList<>();

        String basePrompt = String.format("""
            目的地：%s
            天数：%d天
            领域：%s
            预算：%s
            偏好：%s

            请提出你专业的规划方案。
            """, destination, days, "你的专业领域",
                budget != null ? budget : "未指定",
                preferences != null ? preferences : "无特别偏好");

        // 景点专家方案
        ReactAgent attractionAgentInstance = attractionAgent.createAgent();
        String attractionProposal = attractionAgentInstance.call(
            "作为景点推荐专家，" + basePrompt.replace("你的专业领域", "景点推荐")).getText();
        proposals.add(new AgentProposal("attraction_agent", "景点推荐", attractionProposal));

        // 住宿专家方案
        ReactAgent hotelAgentInstance = hotelAgent.createAgent();
        String hotelProposal = hotelAgentInstance.call(
            "作为住宿推荐专家，" + basePrompt.replace("你的专业领域", "住宿推荐")).getText();
        proposals.add(new AgentProposal("hotel_agent", "住宿推荐", hotelProposal));

        // 交通专家方案
        ReactAgent transportAgentInstance = transportAgent.createAgent();
        String transportProposal = transportAgentInstance.call(
            "作为交通规划专家，" + basePrompt.replace("你的专业领域", "交通规划")).getText();
        proposals.add(new AgentProposal("transport_agent", "交通规划", transportProposal));

        return proposals;
    }

    /**
     * 收集各Agent对方案的评论
     */
    private List<AgentComment> collectComments(AgentProposal targetProposal,
                                                 List<AgentProposal> allProposals,
                                                 int round) throws GraphRunnerException {
        List<AgentComment> comments = new ArrayList<>();

        // 创建评论Agent
        ReactAgent commentator = ReactAgent.builder()
            .name("debate_commentator")
            .model(chatModel)
            .systemPrompt("""
                你是旅游规划辩论专家。从你的专业角度评论其他专家的方案。

                评论要求：
                1. 指出方案的优点
                2. 指出可能的不足或风险
                3. 提出改进建议
                4. 考虑与其他方案的协调性

                输出格式：
                STRENGTHS: 方案优点
                WEAKNESSES: 方案不足
                SUGGESTIONS: 改进建议
                """)
            .build();

        for (AgentProposal proposal : allProposals) {
            if (!proposal.agentId().equals(targetProposal.agentId())) {
                String commentPrompt = String.format("""
                    你是%s专家，请评论以下%s专家的方案：

                    ## 方案内容
                    %s

                    ## 你的方案（作为参考）
                    %s

                    请从专业角度发表评论。
                    """, proposal.domain(), targetProposal.domain(),
                        targetProposal.content(), proposal.content());

                String commentResult = commentator.call(commentPrompt).getText();
                comments.add(new AgentComment(
                    proposal.agentId(),
                    targetProposal.agentId(),
                    round,
                    commentResult
                ));
            }
        }

        return comments;
    }

    /**
     * 根据评论修正方案
     */
    private List<AgentProposal> reviseProposals(List<AgentProposal> proposals,
                                                  List<AgentComment> comments,
                                                  int round) throws GraphRunnerException {
        List<AgentProposal> revisedProposals = new ArrayList<>();

        for (AgentProposal proposal : proposals) {
            // 收集针对该方案的评论
            List<AgentComment> proposalComments = comments.stream()
                .filter(c -> c.targetAgent().equals(proposal.agentId()))
                .toList();

            if (proposalComments.isEmpty()) {
                revisedProposals.add(proposal);
                continue;
            }

            // 创建修正Agent
            ReactAgent reviser = ReactAgent.builder()
                .name("proposal_reviser")
                .model(chatModel)
                .systemPrompt("""
                    你是旅游规划修正专家。根据其他专家的评论意见，优化你的方案。

                    修正要求：
                    1. 考虑其他专家的建议
                    2. 解决指出的问题
                    3. 保持方案的专业性
                    4. 提高与其他方案的协调性
                    """)
                .build();

            StringBuilder commentsText = new StringBuilder();
            for (AgentComment comment : proposalComments) {
                commentsText.append("\n---\n");
                commentsText.append("来自").append(comment.sourceAgent()).append("的评论:\n");
                commentsText.append(comment.content()).append("\n");
            }

            String revisePrompt = String.format("""
                请根据其他专家的评论优化你的方案：

                ## 你的原始方案
                %s

                ## 其他专家的评论
                %s

                请输出优化后的方案。
                """, proposal.content(), commentsText.toString());

            String revisedContent = reviser.call(revisePrompt).getText();
            revisedProposals.add(new AgentProposal(
                proposal.agentId(),
                proposal.domain(),
                revisedContent
            ));
        }

        return revisedProposals;
    }

    /**
     * 检查是否达成共识
     */
    private boolean checkConsensus(List<AgentProposal> proposals) throws GraphRunnerException {
        ReactAgent consensusChecker = ReactAgent.builder()
            .name("consensus_checker")
            .model(chatModel)
            .systemPrompt("""
                你是共识判定专家。判断各专家的方案是否已经足够协调，可以整合。

                输出格式：
                CONSENSUS: true/false
                REASON: 判断理由
                """)
            .build();

        StringBuilder prompt = new StringBuilder("请判断以下方案是否已达成共识:\n\n");
        for (AgentProposal proposal : proposals) {
            prompt.append("## ").append(proposal.domain()).append("方案\n");
            prompt.append(proposal.content()).append("\n\n");
        }

        String response = consensusChecker.call(prompt.toString()).getText();
        return response.contains("CONSENSUS: true") || response.contains("CONSENSUS:true");
    }

    /**
     * 生成共识整合方案
     */
    private String generateConsensusPlan(List<AgentProposal> proposals) throws GraphRunnerException {
        ReactAgent finalAggregator = ReactAgent.builder()
            .name("consensus_aggregator")
            .model(chatModel)
            .systemPrompt("""
                你是旅游规划整合专家。将经过辩论协商的各专家方案整合为最终方案。

                整合要求：
                1. 融合各专家的核心建议
                2. 解决所有已识别的问题
                3. 确保方案协调一致
                4. 输出清晰的执行计划
                """)
            .build();

        StringBuilder prompt = new StringBuilder("请整合以下辩论后的方案:\n\n");
        for (AgentProposal proposal : proposals) {
            prompt.append("## ").append(proposal.domain()).append("方案\n");
            prompt.append(proposal.content()).append("\n\n");
        }

        return finalAggregator.call(prompt.toString()).getText();
    }

    // Types
    public record AgentProposal(String agentId, String domain, String content) {}
    public record AgentComment(String sourceAgent, String targetAgent, int round, String content) {}
    public record DebateRound(int roundNumber, List<AgentComment> comments, List<AgentProposal> revisedProposals) {}
    public record DebatePlanResult(List<DebateRound> debateRounds, String finalPlan) {}
}