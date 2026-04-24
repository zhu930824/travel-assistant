package cn.sdh.travel.agent.autogen;

/**
 * 对话结果
 */
public record GroupChatResult(
    ConversationState state,
    String finalPlan,
    String terminationReason,
    int totalRounds
) {}
