package cn.sdh.travel.agent.expert;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

/**
 * 景点推荐专家Agent
 * 负责根据用户需求推荐合适的景点和游览路线
 */
@Component
@RequiredArgsConstructor
public class AttractionAgent {

    private final ChatModel chatModel;

    private static final String SYSTEM_PROMPT = """
        你是一个旅行景点推荐专家。

        职责：
        - 根据用户的目的地、天数、偏好，推荐合适的景点
        - 安排合理的游览顺序
        - 提供景点开放时间、门票价格等信息
        - 给出游览建议和注意事项

        输出格式要求：
        ## 景点推荐

        ### 第1天
        - 景点名称：xxx
          - 开放时间：xxx
          - 门票价格：xxx
          - 建议游览时长：xxx
          - 推荐理由：xxx

        ### 第2天
        ...

        注意事项：
        1. xxx
        2. xxx

        请直接输出推荐结果，不要有多余的解释。
        """;

    /**
     * 创建景点推荐Agent实例
     */
    public ReactAgent createAgent() {
        return ReactAgent.builder()
            .name("attraction_agent")
            .model(chatModel)
            .description("景点推荐专家，善于根据用户的需求推荐各种适合游玩的景点")
            .systemPrompt(SYSTEM_PROMPT)
            .outputKey("attraction_plan")
            .enableLogging(true)
            .build();
    }
}
