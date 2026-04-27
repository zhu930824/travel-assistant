package cn.sdh.travel.agent.autogen;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 对话状态管理
 * 支持JSON序列化/反序列化，用于checkpoint持久化
 */
@Getter
@Setter
public class ConversationState {

    @JsonProperty("messages")
    private final List<ConversationMessage> messages = new ArrayList<>();

    @JsonProperty("currentRound")
    private int currentRound = 0;

    @JsonProperty("lastSpeaker")
    private String lastSpeaker;

    @JsonProperty("terminated")
    private boolean terminated = false;

    @JsonProperty("terminationReason")
    private String terminationReason;

    @JsonIgnore
    private final Instant startTime;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

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

    /**
     * 序列化为JSON
     */
    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化ConversationState失败", e);
        }
    }

    /**
     * 从JSON反序列化
     */
    public static ConversationState fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return new ConversationState();
        }
        try {
            return OBJECT_MAPPER.readValue(json, ConversationState.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("反序列化ConversationState失败", e);
        }
    }

    /**
     * 从消息列表JSON创建状态（用于从数据库加载）
     */
    public static ConversationState fromMessagesJson(String messagesJson) {
        ConversationState state = new ConversationState();
        if (messagesJson != null && !messagesJson.isEmpty()) {
            try {
                List<ConversationMessage> msgs = OBJECT_MAPPER.readValue(
                    messagesJson,
                    new TypeReference<List<ConversationMessage>>() {}
                );
                state.getMessages().addAll(msgs);
            } catch (JsonProcessingException e) {
                // 忽略解析错误，返回空状态
            }
        }
        return state;
    }
}
