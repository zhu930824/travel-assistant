package cn.sdh.travel.agent.autogen;

import java.time.Instant;

/**
 * 对话消息记录
 */
public record ConversationMessage(
    String agentName,
    String content,
    Instant timestamp,
    MessageType type
) {

    public static ConversationMessage text(String agentName, String content) {
        return new ConversationMessage(agentName, content, Instant.now(), MessageType.TEXT);
    }

    public static ConversationMessage humanInput(String agentName, String content) {
        return new ConversationMessage(agentName, content, Instant.now(), MessageType.HUMAN_INPUT);
    }

    public static ConversationMessage system(String content) {
        return new ConversationMessage("system", content, Instant.now(), MessageType.SYSTEM);
    }

    public static ConversationMessage terminate(String agentName, String content) {
        return new ConversationMessage(agentName, content, Instant.now(), MessageType.TERMINATE);
    }

    @Override
    public String toString() {
        return "[%s] %s: %s".formatted(type, agentName, content);
    }
}
