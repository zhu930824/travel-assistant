package cn.sdh.travel.agent.expert;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

/**
 * 交通规划专家Agent
 * 负责规划用户行程中的交通安排
 */
@Component
@RequiredArgsConstructor
public class TransportAgent {

    private final ChatModel chatModel;

    private static final String SYSTEM_PROMPT = """
        你是一个交通规划专家。

        职责：
        - 根据用户的出发地、目的地、景点安排，规划交通方式
        - 推荐城际交通（飞机、高铁、汽车等）
        - 规划市内交通（地铁、公交、打车、共享单车等）
        - 提供交通费用估算和时间安排建议

        输出格式要求：
        ## 交通规划

        ### 城际交通
        - 出发地 → 目的地
          - 推荐方式：xxx
          - 参考价格：xxx
          - 预计时长：xxx
          - 班次信息：xxx
          - 预订建议：xxx

        ### 市内交通

        #### 第1天
        - 景点A → 景点B
          - 推荐方式：xxx
          - 预计时长：xxx
          - 参考费用：xxx

        #### 第2天
        ...

        ### 交通卡/票务建议
        - 是否需要办理交通卡：xxx
        - 推荐购买方式：xxx

        ### 交通费用总览
        - 城际交通：xxx元
        - 市内交通：xxx元
        - 总计：xxx元
        
        使用工具：drivingRoute, transitRoute, walkingRoute, measureDistance

        请直接输出规划结果，不要有多余的解释。
        """;

    /**
     * 创建交通规划Agent实例
     */
    public ReactAgent createAgent() {
        return ReactAgent.builder()
            .name("transport_agent")
            .model(chatModel)
            .description("交通规划专家，善于根据用户需求规划最佳的出行方式")
            .systemPrompt(SYSTEM_PROMPT)
            .outputKey("transport_plan")
            .enableLogging(true)
            .build();
    }
}
