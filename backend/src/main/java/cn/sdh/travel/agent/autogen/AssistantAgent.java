package cn.sdh.travel.agent.autogen;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;

/**
 * AI 助手 Agent
 * 基于 LLM 和工具的专业 Agent，可参与 AutoGen 对话
 *
 * 特点:
 * 1. 使用 ReactAgent 执行工具调用
 * 2. 可以看到完整的对话历史
 * 3. 根据对话上下文生成专业回复
 * 4. 不会请求人工输入
 */
@Slf4j
public class AssistantAgent extends ConversableAgent {

    private AssistantAgent(String name, String description, String systemPrompt,
                           ChatModel chatModel, List<ToolCallback> tools) {
        super(name, description, systemPrompt, chatModel, tools);
    }

    @Override
    public String receive(String message, ConversableAgent sender, ConversationState state) {
        log.info("[{}] 收到来自 [{}] 的消息: {}", name, sender.getName(), truncate(message, 100));

        ConversationMessage receivedMsg = ConversationMessage.text(sender.getName(), message);
        conversationHistory.add(receivedMsg);

        String reply = generateReply(message, state);

        ConversationMessage replyMsg = ConversationMessage.text(name, reply);
        conversationHistory.add(replyMsg);
        state.addMessage(replyMsg);

        return reply;
    }

    @Override
    protected String generateReply(String message, ConversationState state) {
        // 优先使用带工具的 ReactAgent
        if (!tools.isEmpty()) {
            return generateReplyWithTools(message, state);
        }
        return callLlm(message);
    }

    private String generateReplyWithTools(String message, ConversationState state) {
        try {
            String contextPrompt = buildContextPrompt(message, state);

            ReactAgent reactAgent = ReactAgent.builder()
                .name(name)
                .model(chatModel)
                .description(description)
                .systemPrompt(systemPrompt)
                .tools(tools.toArray(new ToolCallback[0]))
                .outputKey(name + "_output")
                .enableLogging(true)
                .build();

            return reactAgent.call(contextPrompt).getText();
        } catch (GraphRunnerException e) {
            log.error("[{}] 工具调用失败，降级为纯 LLM 回复: {}", name, e.getMessage());
            return callLlm(message);
        }
    }

    private String buildContextPrompt(String currentMessage, ConversationState state) {
        StringBuilder sb = new StringBuilder();

        sb.append("以下是其他专家和用户的对话历史:\n\n");
        for (ConversationMessage msg : state.getMessages()) {
            if (!msg.agentName().equals(name)) {
                sb.append(msg.toString()).append("\n");
            }
        }

        sb.append("\n当前需要你回应的消息:\n");
        sb.append(currentMessage).append("\n\n");

        sb.append("请结合对话历史和你的专业知识生成回复。");
        sb.append("如果其他专家已经提到了相关信息，请注意协调和补充，避免重复。");

        return sb.toString();
    }

    @Override
    public boolean shouldRequestHumanInput() {
        return false;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String description;
        private String systemPrompt;
        private ChatModel chatModel;
        private List<ToolCallback> tools = List.of();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder systemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }

        public Builder chatModel(ChatModel chatModel) {
            this.chatModel = chatModel;
            return this;
        }

        public Builder tools(List<ToolCallback> tools) {
            this.tools = tools;
            return this;
        }

        public AssistantAgent build() {
            return new AssistantAgent(name, description, systemPrompt, chatModel, tools);
        }
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "null";
        return text.length() > maxLen ? text.substring(0, maxLen) + "..." : text;
    }
}
