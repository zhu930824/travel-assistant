package cn.sdh.travel.agent.autogen;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * 可对话 Agent 基类
 * 所有参与 AutoGen 对话的 Agent 都继承此类
 *
 * 核心能力:
 * 1. 消息收发 - 与其他 Agent 交换信息
 * 2. 对话历史 - 维护自身可见的对话记录
 * 3. 生成回复 - 基于 LLM 或用户输入生成回复
 */
@Slf4j
@Getter
public abstract class ConversableAgent {

    protected final String name;
    protected final String description;
    protected final String systemPrompt;
    protected final ChatModel chatModel;
    protected final List<ToolCallback> tools;
    protected final List<ConversationMessage> conversationHistory = new ArrayList<>();

    protected ConversableAgent(String name, String description, String systemPrompt,
                                ChatModel chatModel, List<ToolCallback> tools) {
        this.name = name;
        this.description = description;
        this.systemPrompt = systemPrompt;
        this.chatModel = chatModel;
        this.tools = tools != null ? tools : List.of();
    }

    /**
     * 发送消息给其他 Agent
     */
    public String send(String message, ConversationState state) {
        log.info("[{}] 发送消息: {}", name, truncate(message, 100));
        ConversationMessage msg = ConversationMessage.text(name, message);
        conversationHistory.add(msg);
        state.addMessage(msg);
        return message;
    }

    /**
     * 接收消息并生成回复
     */
    public abstract String receive(String message, ConversableAgent sender, ConversationState state);

    /**
     * 生成回复（子类实现）
     */
    protected abstract String generateReply(String message, ConversationState state);

    /**
     * 是否请求人工输入
     */
    public abstract boolean shouldRequestHumanInput();

    /**
     * 基于 LLM 生成回复的通用方法
     */
    protected String callLlm(String userMessage) {
        StringBuilder promptBuilder = new StringBuilder();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            promptBuilder.append(systemPrompt).append("\n\n");
        }

        promptBuilder.append("对话历史:\n");
        for (ConversationMessage msg : conversationHistory) {
            promptBuilder.append(msg.toString()).append("\n");
        }

        promptBuilder.append("\n当前消息: ").append(userMessage);
        promptBuilder.append("\n\n请根据上下文生成回复。");

        Prompt prompt = new Prompt(promptBuilder.toString());
        return chatModel.call(prompt).getResult().getOutput().getText();
    }

    /**
     * 重置对话历史
     */
    public void reset() {
        conversationHistory.clear();
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "null";
        return text.length() > maxLen ? text.substring(0, maxLen) + "..." : text;
    }
}
