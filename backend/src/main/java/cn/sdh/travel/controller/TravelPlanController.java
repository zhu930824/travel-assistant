package cn.sdh.travel.controller;

import cn.sdh.travel.agent.supervisor.TravelSupervisorPlanAgent;
import cn.sdh.travel.common.context.PlanContext;
import cn.sdh.travel.common.context.UserContext;
import cn.sdh.travel.common.exception.BusinessException;
import cn.sdh.travel.common.result.Result;
import cn.sdh.travel.entity.dto.request.TravelPlanRequest;
import cn.sdh.travel.entity.dto.response.AgentOutputMessage;
import cn.sdh.travel.entity.dto.response.PlanDataResponse;
import cn.sdh.travel.service.PlanRecordService;
import cn.sdh.travel.service.UserService;
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
import java.util.concurrent.ConcurrentHashMap;

/**
 * 旅游规划控制器 - SSE流式接口
 */
@Slf4j
@RestController
@RequestMapping("/api/travel")
@RequiredArgsConstructor
public class TravelPlanController {

    private final TravelSupervisorPlanAgent supervisorPlanAgent;
    private final UserService userService;
    private final PlanRecordService planRecordService;

    private static final Map<String, Long> sessionRecordMap = new ConcurrentHashMap<>();

    @PostMapping(value = "/plan/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> planTravelStream(@Valid @RequestBody TravelPlanRequest request) throws GraphRunnerException {
        log.info("收到规划请求: destination={}, days={}, budget={}, preferences={}",
            request.getDestination(), request.getDays(), request.getBudget(), request.getPreferencesString());

        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }

        if (!userService.canPlan(userId)) {
            throw new BusinessException("本月规划次数已达上限，请升级会员");
        }

        String preferencesStr = request.getPreferences() != null ?
            JSON.toJSONString(request.getPreferences()) : null;
        Long recordId = planRecordService.createRecord(
            userId,
            request.getDestination(),
            request.getDays(),
            request.getBudget(),
            preferencesStr
        );

        String sessionId = UUID.randomUUID().toString();
        sessionRecordMap.put(sessionId, recordId);

        Agent supervisorAgent = supervisorPlanAgent.createAgent(sessionId);

        String prompt = buildPrompt(request);

        RunnableConfig config = RunnableConfig.builder()
            .threadId(sessionId)
            .addMetadata("sessionId", sessionId)
            .addMetadata("recordId", recordId)
            .build();

        Flux<NodeOutput> agentStream = supervisorAgent.stream(prompt, config);

        return Flux.create(sink -> {
            PlanContext.set(sink);
            PlanContext.setSessionEmitter(sessionId, sink);

            sink.next(ServerSentEvent.<String>builder()
                .event("session_start")
                .data(JSON.toJSONString(Map.of("sessionId", sessionId, "recordId", recordId)))
                .build());

            log.info("【SSE连接】已建立连接，sessionId: {}, recordId: {}", sessionId, recordId);

            agentStream.subscribe(
                nodeOutput -> handleNodeOutput(nodeOutput, sink, sessionId),
                error -> handleError(error, sink, sessionId, recordId),
                () -> handleComplete(sink, sessionId, recordId)
            );
        });
    }

    private void handleNodeOutput(NodeOutput nodeOutput,
                                  reactor.core.publisher.FluxSink<ServerSentEvent<String>> sink,
                                  String sessionId) {
        String node = nodeOutput.node();
        log.debug("【监督者输出】node: {}", node);

        if (nodeOutput instanceof StreamingOutput<?> streamingOutput) {
            Message msg = streamingOutput.message();

            if (msg instanceof AssistantMessage assistantMessage) {
                if (!assistantMessage.hasToolCalls()) {
                    String text = msg.getText();
                    if (text != null && !text.trim().isEmpty()) {
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

    private void handleError(Throwable error,
                             reactor.core.publisher.FluxSink<ServerSentEvent<String>> sink,
                             String sessionId,
                             Long recordId) {
        log.error("流式规划过程中发生错误, sessionId: {}", sessionId, error);

        planRecordService.updateRecordResult(recordId, null, 0);

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

    private void handleComplete(reactor.core.publisher.FluxSink<ServerSentEvent<String>> sink,
                                String sessionId,
                                Long recordId) {
        log.info("【SSE连接】规划完成, sessionId: {}", sessionId);

        PlanContext.SessionState state = PlanContext.getOrCreateSessionState(sessionId);
        String planContent = state.getContent("supervisor");

        planRecordService.updateRecordResult(recordId, planContent, 1);

        Map<String, Object> completeData = new HashMap<>();
        completeData.put("sessionId", sessionId);
        completeData.put("recordId", recordId);
        completeData.put("status", "completed");

        sink.next(ServerSentEvent.<String>builder()
            .event("complete")
            .data(JSON.toJSONString(completeData))
            .build());

        sink.complete();
        cleanup(sessionId);
    }

    private void cleanup(String sessionId) {
        PlanContext.removeSessionEmitter(sessionId);
        PlanContext.remove();
        sessionRecordMap.remove(sessionId);
        log.debug("【资源清理】已清理sessionId: {}", sessionId);
    }

    private String buildPrompt(TravelPlanRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请帮我制定一个旅行计划：\n");
        prompt.append("- 目的地：").append(request.getDestination()).append("\n");
        prompt.append("- 天数：").append(request.getDays()).append("天\n");

        if (request.getBudget() != null && !request.getBudget().isEmpty()) {
            prompt.append("- 预算：").append(request.getBudget()).append("\n");
        }

        if (request.getPreferences() != null && !request.getPreferences().isEmpty()) {
            prompt.append("- 偏好：").append(request.getPreferencesString()).append("\n");
        }

        return prompt.toString();
    }

    @GetMapping("/plan/{id}/data")
    public Result<PlanDataResponse> getPlanData(@PathVariable Long id) {
        log.info("获取结构化行程数据: recordId={}", id);

        String planDataJson = planRecordService.getPlanData(id);
        if (planDataJson == null || planDataJson.isEmpty()) {
            return Result.error("行程数据不存在");
        }

        try {
            PlanDataResponse response = JSON.parseObject(planDataJson, PlanDataResponse.class);
            return Result.success(response);
        } catch (Exception e) {
            log.error("解析行程数据失败", e);
            return Result.error("行程数据格式错误");
        }
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "travel-assistant");
        return result;
    }
}
