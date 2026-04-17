package cn.sdh.travel.config.factory.strategy;


import cn.sdh.travel.config.factory.ModelProvider;
import cn.sdh.travel.entity.domain.ModelConfig;

/**
 * 模型创建策略接口
 * 定义创建模型的标准方法
 *
 * @param <M> 模型类型
 */
public interface ModelCreationStrategy<M> {

    /**
     * 获取此策略对应的模型提供者
     */
    ModelProvider getProvider();

    /**
     * 创建模型实例
     *
     * @param config 模型配置
     * @param defaultApiKey 默认 API Key（当配置中未指定时使用）
     * @return 模型实例
     */
    M createModel(ModelConfig config, String defaultApiKey);

    /**
     * 创建默认模型实例
     *
     * @param modelName 模型名称
     * @param apiKey API Key
     * @return 模型实例
     */
    M createDefaultModel(String modelName, String apiKey);
}
