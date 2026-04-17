package cn.sdh.travel.agent.supervisor;

import cn.sdh.travel.agent.expert.AttractionAgent;
import cn.sdh.travel.agent.expert.HotelAgent;
import cn.sdh.travel.agent.expert.TransportAgent;
import cn.sdh.travel.agent.tool.AgentTool;
import com.alibaba.cloud.ai.graph.agent.Agent;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

/**
 * 旅游规划监督者Agent
 * 负责理解用户需求，协调调度各专家Agent
 */
@Component
@RequiredArgsConstructor
public class TravelSupervisorPlanAgent {

    private final AttractionAgent attractionAgent;
    private final HotelAgent hotelAgent;
    private final TransportAgent transportAgent;
    private final ChatModel chatModel;

    private static final String SYSTEM_PROMPT = """
        你是一个旅行规划监督者，你的任务是帮助用户制定旅行计划。

        你可以协调以下专家：
        - attraction_agent：负责推荐景点、安排游览路线。
        - transport_agent：负责规划城市间交通和市内交通。
        - hotel_agent：负责推荐酒店、民宿等住宿。

        工作流程：
        1. 首先调用 attraction_agent 来推荐景点、安排旅游路线
        2. 根据推荐的景点调用 transport_agent 规划交通
        3. 根据推荐的景点调用 hotel_agent 规划住宿
        4. 最后将所有专家输出的内容整合，输出完整的旅行方案

        输出格式要求：
        ## 完整旅行方案

        ### 目的地概况
        简要介绍目的地特色...

        ### 行程安排

        #### 第1天
        - 上午：xxx
        - 下午：xxx
        - 晚上：xxx
        - 住宿：xxx
        - 交通：xxx

        #### 第2天
        ...

        ### 费用估算
        - 门票：xxx元
        - 住宿：xxx元
        - 交通：xxx元
        - 餐饮：xxx元
        - 总计：xxx元

        ### 温馨提示
        1. xxx
        2. xxx

        请确保输出的方案完整、详细、有条理。
        """;

    /**
     * 创建监督者Agent实例
     * @param sessionId 会话ID，用于SSE推送
     */
    public Agent createAgent(String sessionId) {
        return ReactAgent.builder()
            .name("supervisor_agent")
            .model(chatModel)
            .systemPrompt(SYSTEM_PROMPT)
            .saver(new MemorySaver())
            .tools(
                AgentTool.create(attractionAgent.createAgent(), sessionId),
                AgentTool.create(hotelAgent.createAgent(), sessionId),
                AgentTool.create(transportAgent.createAgent(), sessionId)
            )
            .enableLogging(true)
            .build();
    }
}