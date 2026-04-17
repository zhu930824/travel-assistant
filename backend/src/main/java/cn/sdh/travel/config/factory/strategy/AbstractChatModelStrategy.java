package cn.sdh.travel.config.factory.strategy;


import cn.sdh.travel.entity.domain.ModelConfig;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;


/**
 * 抽象 Chat 模型创建策略基类
 * 提供 OpenAI 兼容模型创建的通用方法
 */
@Slf4j
public abstract class AbstractChatModelStrategy implements ModelCreationStrategy<ChatModel> {

    @Override
    public ChatModel createModel(ModelConfig config, String defaultApiKey) {
        String baseUrl = resolveBaseUrl(config);
        String apiKey = resolveApiKey(config, defaultApiKey);
        String modelName = config.getModelId();

        log.info("创建 Chat 模型: provider={}, baseUrl={}, model={}", getProvider(), baseUrl, modelName);

        return doCreateModel(baseUrl, apiKey, modelName, config);
    }

    @Override
    public ChatModel createDefaultModel(String modelName, String apiKey) {
        log.info("使用默认配置创建 Chat 模型: provider={}, model={}", getProvider(), modelName);
        return doCreateModel(getProvider().getDefaultBaseUrl(), apiKey, modelName, null);
    }

    /**
     * 子类实现具体的模型创建逻辑
     */
    protected ChatModel doCreateModel(String baseUrl, String apiKey, String modelName, ModelConfig config) {
        // 默认使用 OpenAI 兼容方式创建
        return createOpenAiCompatibleModel(baseUrl, apiKey, modelName, config);
    }

    /**
     * 创建 OpenAI 兼容的 Chat 模型
     */
    protected ChatModel createOpenAiCompatibleModel(String baseUrl, String apiKey, String modelName, ModelConfig config) {
        // 脱敏 API Key 用于日志
        String maskedApiKey = maskApiKey(apiKey);
        log.info("创建 DashScope ChatModel - baseUrl: {}, model: {}, apiKey: {}..., temperature: {}, maxTokens: {}",
                baseUrl, modelName, maskedApiKey,
                config != null ? config.getTemperature() : null,
                config != null ? config.getMaxTokens() : null);

        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();

        // 判断是否为多模态模型，默认 false
        boolean isMultiModel = config != null && config.getIsMultiModel() != null && config.getIsMultiModel() == 1;

        DashScopeChatOptions.DashScopeChatOptionsBuilder builder = DashScopeChatOptions.builder()
                .model(modelName)
                .multiModel(isMultiModel);

        if (config != null) {
            if (config.getTemperature() != null) {
                builder.temperature(config.getTemperature());
            }
            if (config.getMaxTokens() != null) {
                builder.maxToken(config.getMaxTokens());
            }
        }

        DashScopeChatOptions options = builder.build();
        log.debug("DashScopeChatOptions: model={}", options.getModel());

        return DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(options)
                .build();
    }

    /**
     * 解析 baseUrl，优先使用配置中的，否则使用默认值
     */
    protected String resolveBaseUrl(ModelConfig config) {
        String baseUrl = config.getBaseUrl();
        if (baseUrl != null && !baseUrl.isEmpty()) {
            return baseUrl;
        }
        return getProvider().getDefaultBaseUrl();
    }

    /**
     * 解析 apiKey，优先使用配置中的，否则使用默认值
     */
    protected String resolveApiKey(ModelConfig config, String defaultApiKey) {
        return config.getApiKey() != null && !config.getApiKey().isEmpty()
                ? config.getApiKey()
                : defaultApiKey;
    }

    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "******";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}