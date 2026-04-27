package cn.sdh.travel.agent.autogen;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;

import java.util.List;
import java.util.function.Function;

/**
 * 用户代理 Agent
 * 代表人类用户参与对话，支持三种输入模式
 *
 * HumanInputMode:
 * - ALWAYS:   每轮都请求用户输入（完全人工参与）
 * - NEVER:    从不请求用户输入，自动生成确认/反馈回复
 * - TERMINATE: 仅在终止条件触发时请求用户确认
 */
@Slf4j
public class UserProxyAgent extends ConversableAgent {

    private final HumanInputMode humanInputMode;
    private final Function<String, String> humanInputProvider;

    private UserProxyAgent(String name, String description, String systemPrompt,
                           ChatModel chatModel, HumanInputMode humanInputMode,
                           Function<String, String> humanInputProvider) {
        super(name, description, systemPrompt, chatModel, List.of());
        this.humanInputMode = humanInputMode;
        this.humanInputProvider = humanInputProvider;
    }

    @Override
    public String receive(String message, ConversableAgent sender, ConversationState state) {
        log.info("[UserProxy] 收到来自 [{}] 的消息: {}", sender.getName(), truncate(message, 100));

        ConversationMessage receivedMsg = ConversationMessage.text(sender.getName(), message);
        conversationHistory.add(receivedMsg);

        // 在生成回复前，如果需要人工输入，先触发暂停回调保存状态
        if (shouldRequestHumanInput() && onRequestHumanInputCallback != null) {
            log.info("[UserProxy] 请求人工输入前，触发暂停回调保存checkpoint");
            onRequestHumanInputCallback.accept(state);
        }

        String reply = generateReply(message, state);

        return reply;
    }

    @Override
    protected String generateReply(String message, ConversationState state) {
        return switch (humanInputMode) {
            case ALWAYS -> requestHumanInput(message, state);
            case NEVER -> generateAutoReply(message, state);
            case TERMINATE -> {
                if (state.isTerminated() || shouldTerminate(message)) {
                    // TERMINATE模式下需要人工输入时，也触发暂停回调
                    if (onRequestHumanInputCallback != null) {
                        log.info("[UserProxy] TERMINATE模式请求人工输入前，触发暂停回调");
                        onRequestHumanInputCallback.accept(state);
                    }
                    yield requestHumanInput(message, state);
                }
                yield generateAutoReply(message, state);
            }
        };
    }

    @Override
    public boolean shouldRequestHumanInput() {
        return humanInputMode == HumanInputMode.ALWAYS;
    }

    /**
     * 请求人类输入
     */
    private String requestHumanInput(String message, ConversationState state) {
        if (humanInputProvider != null) {
            String prompt = "AI助手说:\n" + message + "\n\n请输入你的反馈（或输入 TERMINATE 结束对话）:";
            String userInput = humanInputProvider.apply(prompt);
            log.info("[UserProxy] 用户输入: {}", truncate(userInput, 100));

            if ("TERMINATE".equalsIgnoreCase(userInput.trim())) {
                return "TERMINATE";
            }

            ConversationMessage humanMsg = ConversationMessage.humanInput(name, userInput);
            conversationHistory.add(humanMsg);
            if (state != null) {
                state.addMessage(humanMsg);
            }

            return userInput;
        }
        return generateAutoReply(message, state);
    }

    /**
     * 自动生成回复（无需人类输入时）
     */
    private String generateAutoReply(String message, ConversationState state) {
        String autoReply = callLlmForAutoReply(message);
        ConversationMessage replyMsg = ConversationMessage.text(name, autoReply);
        conversationHistory.add(replyMsg);
        if (state != null) {
            state.addMessage(replyMsg);
        }
        return autoReply;
    }

    private String callLlmForAutoReply(String message) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("你是一个旅行规划的用户代理。根据专家的建议，提供简短的反馈。\n\n");
        promptBuilder.append("专家建议:\n").append(message).append("\n\n");
        promptBuilder.append("请简短回复是否满意，或提出修改建议。如果满意，回复'方案已完成'。");

        org.springframework.ai.chat.prompt.Prompt prompt =
            new org.springframework.ai.chat.prompt.Prompt(promptBuilder.toString());
        return chatModel.call(prompt).getResult().getOutput().getText();
    }

    private boolean shouldTerminate(String message) {
        if (message == null) return false;
        String lower = message.toLowerCase();
        return lower.contains("terminate") || lower.contains("方案已完成")
            || lower.contains("满意") || lower.contains("不需要修改");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name = "user_proxy";
        private String description = "用户代理";
        private String systemPrompt = "你是用户的代理，代表用户参与旅游规划对话。";
        private ChatModel chatModel;
        private HumanInputMode humanInputMode = HumanInputMode.TERMINATE;
        private Function<String, String> humanInputProvider;

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

        public Builder humanInputMode(HumanInputMode mode) {
            this.humanInputMode = mode;
            return this;
        }

        public Builder humanInputProvider(Function<String, String> provider) {
            this.humanInputProvider = provider;
            return this;
        }

        public UserProxyAgent build() {
            return new UserProxyAgent(name, description, systemPrompt, chatModel,
                humanInputMode, humanInputProvider);
        }
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "null";
        return text.length() > maxLen ? text.substring(0, maxLen) + "..." : text;
    }
}
