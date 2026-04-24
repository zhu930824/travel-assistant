package cn.sdh.travel.agent.autogen;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;

/**
 * 对话结果
 */
public record GroupChatResult(
    ConversationState state,
    String finalPlan,
    String terminationReason,
    int totalRounds
) {}

/**
 * 群聊管理器
 * 负责选择下一个发言者和管理对话流程
 */
@Slf4j
public class GroupChatManager {

    private final SpeakerSelectionMode selectionMode;

    /**
     * 发言者选择模式
     */
    public enum SpeakerSelectionMode {
        ROUND_ROBIN,    // 轮询
        RANDOM,         // 随机
        SMART           // 智能选择（让 LLM 决定）
    }

    public GroupChatManager(SpeakerSelectionMode mode) {
        this.selectionMode = mode;
    }

    /**
     * 选择下一个发言者
     */
    public ConversableAgent selectNextSpeaker(List<ConversableAgent> agents,
                                               ConversationState state) {
        return switch (selectionMode) {
            case ROUND_ROBIN -> selectRoundRobin(agents, state);
            case RANDOM -> selectRandom(agents);
            case SMART -> selectSmart(agents, state);
        };
    }

    private ConversableAgent selectRoundRobin(List<ConversableAgent> agents,
                                               ConversationState state) {
        int idx = state.getCurrentRound() % agents.size();
        return agents.get(idx);
    }

    private ConversableAgent selectRandom(List<ConversableAgent> agents) {
        return agents.get(new Random().nextInt(agents.size()));
    }

    private ConversableAgent selectSmart(List<ConversableAgent> agents,
                                          ConversationState state) {
        // 简化实现：轮询，但跳过刚刚发言的 Agent
        ConversableAgent lastSpeaker = null;
        for (int i = agents.size() - 1; i >= 0; i--) {
            if (agents.get(i).getName().equals(state.getLastSpeaker())) {
                lastSpeaker = agents.get(i);
                break;
            }
        }

        if (lastSpeaker != null && agents.size() > 1) {
            int lastIdx = agents.indexOf(lastSpeaker);
            return agents.get((lastIdx + 1) % agents.size());
        }
        return selectRoundRobin(agents, state);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private SpeakerSelectionMode mode = SpeakerSelectionMode.ROUND_ROBIN;

        public Builder roundRobin() {
            this.mode = SpeakerSelectionMode.ROUND_ROBIN;
            return this;
        }

        public Builder random() {
            this.mode = SpeakerSelectionMode.RANDOM;
            return this;
        }

        public Builder smart() {
            this.mode = SpeakerSelectionMode.SMART;
            return this;
        }

        public GroupChatManager build() {
            return new GroupChatManager(mode);
        }
    }
}
