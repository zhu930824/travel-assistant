package cn.sdh.travel.controller;

import cn.sdh.travel.agent.supervisor.TravelSupervisorPlanAgent;
import cn.sdh.travel.common.context.PlanContext;
import cn.sdh.travel.entity.dto.request.TravelPlanRequest;
import cn.sdh.travel.entity.dto.response.AgentOutputMessage;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.Agent;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.alibaba.fastjson2.JSON;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 旅游规划控制器 - SSE流式接口
 */
@Slf4j
@RestController
@RequestMapping("/api/travel")
@RequiredArgsConstructor
public class TravelPlanController {

    private final TravelSupervisorPlanAgent supervisorPlanAgent;

    /**
     * 流式旅游规划接口
     * 通过SSE实时推送各Agent的规划过程
     */
    @PostMapping(value = "/plan/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> planTravelStream(@Valid @RequestBody TravelPlanRequest request) throws GraphRunnerException {
        log.info("收到规划请求: destination={}, days={}, budget={}, preferences={}",
            request.getDestination(), request.getDays(), request.getBudget(), request.getPreferences());

        String sessionId = UUID.randomUUID().toString();
        Agent supervisorAgent = supervisorPlanAgent.createAgent(sessionId);

        String prompt = buildPrompt(request);

        RunnableConfig config = RunnableConfig.builder()
            .threadId(sessionId)
            .addMetadata("sessionId", sessionId)
            .build();

        Flux<NodeOutput> agentStream = supervisorAgent.stream(prompt, config);

        return Flux.create(sink -> {
            // 存储SSE连接
            PlanContext.set(sink);
            PlanContext.setSessionEmitter(sessionId, sink);

            // 发送会话开始事件
            sink.next(ServerSentEvent.<String>builder()
                .event("session_start")
                .data(JSON.toJSONString(Map.of("sessionId", sessionId)))
                .build());

            log.info("【SSE连接】已建立连接，sessionId: {}", sessionId);

            // 异步处理Agent流
            agentStream.subscribe(
                nodeOutput -> handleNodeOutput(nodeOutput, sink, sessionId),
                error -> handleError(error, sink, sessionId),
                () -> handleComplete(sink, sessionId)
            );
        });
    }

    /**
     * 处理节点输出
     */
    private void handleNodeOutput(NodeOutput nodeOutput,
                                  reactor.core.publisher.FluxSink<ServerSentEvent<String>> sink,
                                  String sessionId) {
        String node = nodeOutput.node();
        log.debug("【监督者输出】node: {}", node);

        // 处理监督者Agent的直接输出
        if (nodeOutput instanceof StreamingOutput<?> streamingOutput) {
            Message msg = streamingOutput.message();

            if (msg instanceof AssistantMessage assistantMessage) {
                if (!assistantMessage.hasToolCalls()) {
                    String text = msg.getText();
                    if (text != null && !text.trim().isEmpty()) {
                        // 更新累积内容
                        PlanContext.SessionState state = PlanContext.getOrCreateSessionState(sessionId);
                        state.appendContent("supervisor", text);

                        AgentOutputMessage message = AgentOutputMessage.builder()
                            .node(node)
                            .agent("supervisor_agent")
                            .stage("supervisor")
                            .contentType("delta")
                            .content(text)
                            .hasContent(true)
                            .build();

                        sink.next(ServerSentEvent.<String>builder()
                            .event("message")
                            .data(JSON.toJSONString(message))
                            .build());
                    }
                }
            }
        }
    }

    /**
     * 处理错误
     */
    private void handleError(Throwable error,
                             reactor.core.publisher.FluxSink<ServerSentEvent<String>> sink,
                             String sessionId) {
        log.error("流式规划过程中发生错误, sessionId: {}", sessionId, error);

        Map<String, String> errorData = new HashMap<>();
        errorData.put("error", error.getMessage());
        errorData.put("sessionId", sessionId);

        sink.next(ServerSentEvent.<String>builder()
            .event("error")
            .data(JSON.toJSONString(errorData))
            .build());

        cleanup(sessionId);
        sink.complete();
    }

    /**
     * 处理完成
     */
    private void handleComplete(reactor.core.publisher.FluxSink<ServerSentEvent<String>> sink,
                                String sessionId) {
        log.info("【SSE连接】规划完成, sessionId: {}", sessionId);

        // 发送完成事件
        Map<String, Object> completeData = new HashMap<>();
        completeData.put("sessionId", sessionId);
        completeData.put("status", "completed");

        sink.next(ServerSentEvent.<String>builder()
            .event("complete")
            .data(JSON.toJSONString(completeData))
            .build());

        sink.complete();
        cleanup(sessionId);
    }

    /**
     * 清理资源
     */
    private void cleanup(String sessionId) {
        PlanContext.removeSessionEmitter(sessionId);
        PlanContext.remove();
        log.debug("【资源清理】已清理sessionId: {}", sessionId);
    }

    /**
     * 构建提示词
     */
    private String buildPrompt(TravelPlanRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请帮我制定一个旅行计划：\n");
        prompt.append("- 目的地：").append(request.getDestination()).append("\n");
        prompt.append("- 天数：").append(request.getDays()).append("天\n");

        if (request.getBudget() != null && !request.getBudget().isEmpty()) {
            prompt.append("- 预算：").append(request.getBudget()).append("\n");
        }

        if (request.getPreferences() != null && !request.getPreferences().isEmpty()) {
            prompt.append("- 偏好：").append(request.getPreferences()).append("\n");
        }

        return prompt.toString();
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "travel-assistant");
        return result;
    }
}
