package cn.sdh.travel.agent.expert;

import cn.sdh.travel.agent.tool.GaodeMapTool;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.stereotype.Component;

/**
 * 住宿推荐专家Agent
 * 负责根据用户需求和景点安排推荐合适的住宿
 */
@Component
@RequiredArgsConstructor
public class HotelAgent {

    private final ChatModel chatModel;

    private final GaodeMapTool gaodeMapTool;

    private static final String SYSTEM_PROMPT = """
            你是一个住宿推荐专家。
            
            职责：
            - 根据用户的景点计划、预算，推荐合适的住宿地点和酒店
            - 考虑交通便利性，推荐最佳住宿区域
            - 提供不同价位的选择（经济型、舒适型、豪华型）
            - 给出预订建议和注意事项
            
            输出格式要求：
            ## 住宿推荐
            
            ### 推荐住宿区域
            1. xxx区域
               - 推荐理由：xxx
               - 交通便利性：xxx
            
            ### 酒店推荐
            
            #### 经济型（预算xxx-xxx元/晚）
            - 酒店名称：xxx
              - 地址：xxx
              - 参考价格：xxx
              - 特色：xxx
            
            #### 舒适型（预算xxx-xxx元/晚）
            ...
            
            #### 豪华型（预算xxx-xxx元/晚）
            ...
            
            预订建议：
            1. xxx
            2. xxx
            
            使用工具：searchHotels, geocode, measureDistance
            
            请直接输出推荐结果，不要有多余的解释。
            """;

    /**
     * 创建住宿推荐Agent实例
     */
    public ReactAgent createAgent() {
        ToolCallback[] toolCallbacks = MethodToolCallbackProvider.builder()
                .toolObjects(gaodeMapTool)
                .build()
                .getToolCallbacks();

        return ReactAgent.builder()
                .name("hotel_agent")
                .model(chatModel)
                .description("住宿推荐专家，善于根据用户需求推荐适合的住宿")
                .systemPrompt(SYSTEM_PROMPT)
                .tools(toolCallbacks)
                .outputKey("hotel_plan")
                .enableLogging(true)
                .build();
    }
}
