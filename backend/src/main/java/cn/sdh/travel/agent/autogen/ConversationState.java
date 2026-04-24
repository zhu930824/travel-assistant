package cn.sdh.travel.agent.autogen;

import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 对话状态管理
 */
public class ConversationState {

    @Getter
    private final List<ConversationMessage> messages = new ArrayList<>();

    @Getter
    private int currentRound = 0;

    @Getter
    private String lastSpeaker;

    @Getter
    private boolean terminated = false;

    @Getter
    private String terminationReason;

    private final Instant startTime;

    public ConversationState() {
        this.startTime = Instant.now();
    }

    public void addMessage(ConversationMessage message) {
        messages.add(message);
        if (message.type() != MessageType.SYSTEM) {
            lastSpeaker = message.agentName();
        }
    }

    public void incrementRound() {
        currentRound++;
    }

    public void terminate(String reason) {
        this.terminated = true;
        this.terminationReason = reason;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public List<ConversationMessage> getMessagesByAgent(String agentName) {
        return messages.stream()
            .filter(m -> m.agentName().equals(agentName))
            .toList();
    }

    public String getLastMessageContent() {
        if (messages.isEmpty()) {
            return null;
        }
        return messages.getLast().content();
    }

    public ConversationMessage getLastMessage() {
        if (messages.isEmpty()) {
            return null;
        }
        return messages.getLast();
    }

    public String formatHistory() {
        StringBuilder sb = new StringBuilder();
        for (ConversationMessage msg : messages) {
            sb.append(msg.toString()).append("\n");
        }
        return sb.toString();
    }

    public void reset() {
        messages.clear();
        currentRound = 0;
        lastSpeaker = null;
        terminated = false;
        terminationReason = null;
    }
}
