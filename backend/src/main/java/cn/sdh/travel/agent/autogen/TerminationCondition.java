package cn.sdh.travel.agent.autogen;

import java.util.List;
import java.util.function.Predicate;

/**
 * 终止条件检测
 */
public class TerminationCondition {

    private final int maxRounds;
    private final List<String> terminationKeywords;
    private final Predicate<ConversationState> customCondition;

    private TerminationCondition(Builder builder) {
        this.maxRounds = builder.maxRounds;
        this.terminationKeywords = builder.terminationKeywords;
        this.customCondition = builder.customCondition;
    }

    public boolean shouldTerminate(ConversationState state) {
        // 检查最大轮数
        if (state.getCurrentRound() >= maxRounds) {
            return true;
        }

        // 检查终止关键词
        ConversationMessage lastMessage = state.getLastMessage();
        if (lastMessage != null && lastMessage.type() != MessageType.SYSTEM) {
            String content = lastMessage.content().toLowerCase();
            for (String keyword : terminationKeywords) {
                if (content.contains(keyword.toLowerCase())) {
                    return true;
                }
            }
        }

        // 检查自定义条件
        if (customCondition != null && customCondition.test(state)) {
            return true;
        }

        // 检查终止消息类型
        if (lastMessage != null && lastMessage.type() == MessageType.TERMINATE) {
            return true;
        }

        return false;
    }

    public String getTerminationReason(ConversationState state) {
        if (state.getCurrentRound() >= maxRounds) {
            return "达到最大对话轮数: " + maxRounds;
        }

        ConversationMessage lastMessage = state.getLastMessage();
        if (lastMessage != null) {
            String content = lastMessage.content().toLowerCase();
            for (String keyword : terminationKeywords) {
                if (content.contains(keyword.toLowerCase())) {
                    return "检测到终止关键词: " + keyword;
                }
            }
            if (lastMessage.type() == MessageType.TERMINATE) {
                return "收到终止信号";
            }
        }

        if (customCondition != null && customCondition.test(state)) {
            return "自定义终止条件触发";
        }

        return "未知原因";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int maxRounds = 10;
        private List<String> terminationKeywords = List.of("TERMINATE", "方案已完成", "满意", "不需要修改");
        private Predicate<ConversationState> customCondition;

        public Builder maxRounds(int maxRounds) {
            this.maxRounds = maxRounds;
            return this;
        }

        public Builder terminationKeywords(List<String> keywords) {
            this.terminationKeywords = keywords;
            return this;
        }

        public Builder addTerminationKeyword(String keyword) {
            this.terminationKeywords = new java.util.ArrayList<>(this.terminationKeywords);
            this.terminationKeywords.add(keyword);
            return this;
        }

        public Builder customCondition(Predicate<ConversationState> condition) {
            this.customCondition = condition;
            return this;
        }

        public TerminationCondition build() {
            return new TerminationCondition(this);
        }
    }
}
