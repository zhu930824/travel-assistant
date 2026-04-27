package cn.sdh.travel.controller;

import cn.sdh.travel.agent.orchestrator.AutoGenOrchestrator;
import cn.sdh.travel.agent.autogen.ConversationMessage;
import cn.sdh.travel.agent.autogen.ConversationState;
import cn.sdh.travel.agent.autogen.GroupChatResult;
import cn.sdh.travel.common.context.UserContext;
import cn.sdh.travel.entity.domain.ConversationCheckpoint;
import cn.sdh.travel.service.CheckpointService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * AutoGen 模式控制器
 * 提供人机协作对话 API
 */
@Slf4j
@RestController
@RequestMapping("/api/autogen")
@RequiredArgsConstructor
public class AutoGenController {

    private final AutoGenOrchestrator autoGenOrchestrator;
    private final CheckpointService checkpointService;

    // 存储活跃的对话会话
    private final ConcurrentHashMap<String, CompletableFuture<String>> pendingInputs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ConversationSession> sessions = new ConcurrentHashMap<>();

    /**
     * 启动 AutoGen 对话
     */
    @PostMapping("/start")
    public AutoGenStartResponse startConversation(@RequestBody AutoGenStartRequest request) {
        log.info("【AutoGen】开始对话: destination={}, days={}", request.getDestination(), request.getDays());

        Long userId = UserContext.getUserId();
        String sessionId = checkpointService.createCheckpoint(
            userId,
            request.getDestination(),
            request.getDays(),
            request.getBudget(),
            request.getPreferences(),
            request.getHumanInputMode()
        );

        ConversationSession session = new ConversationSession();
        session.setSessionId(sessionId);
        session.setDestination(request.getDestination());
        session.setDays(request.getDays());
        session.setBudget(request.getBudget());
        session.setPreferences(request.getPreferences());
        session.setHumanInputMode(request.getHumanInputMode() != null ? request.getHumanInputMode() : "TERMINATE");
        session.setStatus("running");
        session.setCreatedAt(System.currentTimeMillis());

        sessions.put(sessionId, session);

        return new AutoGenStartResponse(sessionId, "对话已启动", session);
    }

    /**
     * 流式执行对话（SSE）
     */
    @GetMapping(value = "/stream/{sessionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamConversation(@PathVariable String sessionId) {
        log.info("【AutoGen】流式对话: sessionId={}", sessionId);

        ConversationSession session = sessions.get(sessionId);
        if (session == null) {
            // 尝试从checkpoint恢复session
            ConversationCheckpoint checkpoint = checkpointService.getCheckpoint(sessionId);
            if (checkpoint == null) {
                SseEmitter emitter = new SseEmitter();
                emitter.completeWithError(new RuntimeException("会话不存在"));
                return emitter;
            }
            // 从checkpoint恢复session
            session = restoreSessionFromCheckpoint(checkpoint);
            sessions.put(sessionId, session);
        }

        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时

        // 异步执行对话
        CompletableFuture.runAsync(() -> {
            try {
                Function<String, String> inputProvider = null;
                Consumer<ConversationState> pauseCallback = (state) -> {
                    try {
                        emitter.send(SseEmitter.event()
                            .name("paused")
                            .data("{\"sessionId\":\"" + sessionId + "\",\"round\":" + state.getCurrentRound() + "}"));
                    } catch (IOException e) {
                        log.error("发送暂停事件失败", e);
                    }
                };

                if ("ALWAYS".equals(session.getHumanInputMode()) ||
                    "TERMINATE".equals(session.getHumanInputMode())) {
                    inputProvider = (prompt) -> {
                        try {
                            // 发送请求用户输入的事件
                            emitter.send(SseEmitter.event()
                                .name("input_required")
                                .data("{\"prompt\":\"" + escapeJson(prompt) + "\"}"));

                            // 等待用户输入
                            CompletableFuture<String> inputFuture = new CompletableFuture<>();
                            pendingInputs.put(sessionId, inputFuture);

                            String input = inputFuture.get(120, TimeUnit.SECONDS);
                            pendingInputs.remove(sessionId);

                            return input;
                        } catch (Exception e) {
                            log.error("等待用户输入失败", e);
                            return "TERMINATE";
                        }
                    };
                }

                AutoGenOrchestrator.AutoGenPlanResult result = autoGenOrchestrator.planWithAutoGen(
                    sessionId,
                    session.getDestination(),
                    session.getDays(),
                    session.getBudget(),
                    session.getPreferences(),
                    inputProvider,
                    pauseCallback
                );

                // 发送对话消息
                for (ConversationMessage msg : result.conversationState().getMessages()) {
                    emitter.send(SseEmitter.event()
                        .name("message")
                        .data(toJson(msg)));
                }

                // 发送最终结果
                emitter.send(SseEmitter.event()
                    .name("complete")
                    .data(toJson(result)));

                emitter.complete();
                session.setStatus("completed");

            } catch (Exception e) {
                log.error("对话执行失败", e);
                try {
                    emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}"));
                } catch (IOException ex) {
                    // ignore
                }
                emitter.completeWithError(e);
                session.setStatus("error");
            }
        });

        emitter.onTimeout(() -> {
            log.warn("【AutoGen】对话超时: sessionId={}", sessionId);
            session.setStatus("timeout");
            pendingInputs.remove(sessionId);
            checkpointService.pauseSession(sessionId);
        });

        emitter.onCompletion(() -> {
            log.info("【AutoGen】对话结束: sessionId={}", sessionId);
        });

        return emitter;
    }

    /**
     * 提交用户输入
     */
    @PostMapping("/input/{sessionId}")
    public AutoGenInputResponse submitInput(@PathVariable String sessionId,
                                             @RequestBody AutoGenInputRequest request) {
        log.info("【AutoGen】收到用户输入: sessionId={}, input={}", sessionId,
            request.getInput().substring(0, Math.min(50, request.getInput().length())));

        CompletableFuture<String> future = pendingInputs.get(sessionId);
        if (future == null) {
            return new AutoGenInputResponse(false, "没有等待中的输入请求");
        }

        future.complete(request.getInput());
        return new AutoGenInputResponse(true, "输入已接收");
    }

    /**
     * 获取会话状态
     */
    @GetMapping("/session/{sessionId}")
    public AutoGenSessionResponse getSession(@PathVariable String sessionId) {
        ConversationSession session = sessions.get(sessionId);
        if (session == null) {
            return new AutoGenSessionResponse(false, null, "会话不存在");
        }
        return new AutoGenSessionResponse(true, session, null);
    }

    /**
     * 终止对话
     */
    @PostMapping("/terminate/{sessionId}")
    public AutoGenInputResponse terminateSession(@PathVariable String sessionId) {
        log.info("【AutoGen】终止对话: sessionId={}", sessionId);

        CompletableFuture<String> future = pendingInputs.get(sessionId);
        if (future != null) {
            future.complete("TERMINATE");
        }

        ConversationSession session = sessions.get(sessionId);
        if (session != null) {
            session.setStatus("terminated");
        }

        return new AutoGenInputResponse(true, "对话已终止");
    }

    /**
     * 快速自动规划（无需人工参与）
     */
    @PostMapping("/auto")
    public AutoGenOrchestrator.AutoGenPlanResult autoPlan(@RequestBody AutoGenStartRequest request) {
        log.info("【AutoGen】自动规划: destination={}", request.getDestination());
        return autoGenOrchestrator.planAuto(
            request.getDestination(),
            request.getDays(),
            request.getBudget(),
            request.getPreferences()
        );
    }

    // ===== Checkpoint API =====

    /**
     * 获取checkpoint详情
     */
    @GetMapping("/checkpoint/{sessionId}")
    public CheckpointResponse getCheckpoint(@PathVariable String sessionId) {
        ConversationCheckpoint checkpoint = checkpointService.getCheckpoint(sessionId);
        if (checkpoint == null) {
            return new CheckpointResponse(false, null, "Checkpoint不存在");
        }
        return new CheckpointResponse(true, checkpoint, null);
    }

    /**
     * 获取用户的活跃checkpoint列表
     */
    @GetMapping("/checkpoint/user")
    public CheckpointListResponse getUserCheckpoints() {
        Long userId = UserContext.getUserId();
        List<ConversationCheckpoint> checkpoints = checkpointService.getUserActiveCheckpoints(userId);
        return new CheckpointListResponse(true, checkpoints, null);
    }

    /**
     * 从checkpoint恢复对话（流式）
     */
    @GetMapping(value = "/checkpoint/{sessionId}/resume", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter resumeFromCheckpoint(@PathVariable String sessionId) {
        log.info("【AutoGen】从checkpoint恢复: sessionId={}", sessionId);

        ConversationCheckpoint checkpoint = checkpointService.getCheckpoint(sessionId);
        if (checkpoint == null) {
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(new RuntimeException("Checkpoint不存在"));
            return emitter;
        }

        ConversationSession session = restoreSessionFromCheckpoint(checkpoint);
        session.setStatus("resuming");
        sessions.put(sessionId, session);

        SseEmitter emitter = new SseEmitter(300000L);

        CompletableFuture.runAsync(() -> {
            try {
                Function<String, String> inputProvider = null;
                Consumer<ConversationState> pauseCallback = (state) -> {
                    try {
                        emitter.send(SseEmitter.event()
                            .name("paused")
                            .data("{\"sessionId\":\"" + sessionId + "\",\"round\":" + state.getCurrentRound() + "}"));
                    } catch (IOException e) {
                        log.error("发送暂停事件失败", e);
                    }
                };

                if ("ALWAYS".equals(session.getHumanInputMode()) ||
                    "TERMINATE".equals(session.getHumanInputMode())) {
                    inputProvider = (prompt) -> {
                        try {
                            emitter.send(SseEmitter.event()
                                .name("input_required")
                                .data("{\"prompt\":\"" + escapeJson(prompt) + "\"}"));

                            CompletableFuture<String> inputFuture = new CompletableFuture<>();
                            pendingInputs.put(sessionId, inputFuture);

                            String input = inputFuture.get(120, TimeUnit.SECONDS);
                            pendingInputs.remove(sessionId);

                            return input;
                        } catch (Exception e) {
                            log.error("等待用户输入失败", e);
                            return "TERMINATE";
                        }
                    };
                }

                AutoGenOrchestrator.AutoGenPlanResult result = autoGenOrchestrator.resumeFromCheckpoint(
                    sessionId, inputProvider, pauseCallback
                );

                for (ConversationMessage msg : result.conversationState().getMessages()) {
                    emitter.send(SseEmitter.event()
                        .name("message")
                        .data(toJson(msg)));
                }

                emitter.send(SseEmitter.event()
                    .name("complete")
                    .data(toJson(result)));

                emitter.complete();
                session.setStatus("completed");

            } catch (Exception e) {
                log.error("恢复对话执行失败", e);
                try {
                    emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}"));
                } catch (IOException ex) {
                    // ignore
                }
                emitter.completeWithError(e);
                session.setStatus("error");
            }
        });

        emitter.onTimeout(() -> {
            log.warn("【AutoGen】恢复对话超时: sessionId={}", sessionId);
            session.setStatus("timeout");
            pendingInputs.remove(sessionId);
            checkpointService.pauseSession(sessionId);
        });

        return emitter;
    }

    /**
     * 删除checkpoint
     */
    @DeleteMapping("/checkpoint/{sessionId}")
    public AutoGenInputResponse deleteCheckpoint(@PathVariable String sessionId) {
        log.info("【AutoGen】删除checkpoint: sessionId={}", sessionId);
        boolean deleted = checkpointService.deleteCheckpoint(sessionId);
        return new AutoGenInputResponse(deleted, deleted ? "Checkpoint已删除" : "Checkpoint不存在");
    }

    // ===== 辅助方法 =====

    private ConversationSession restoreSessionFromCheckpoint(ConversationCheckpoint checkpoint) {
        ConversationSession session = new ConversationSession();
        session.setSessionId(checkpoint.getSessionId());
        session.setDestination(checkpoint.getDestination());
        session.setDays(checkpoint.getDays());
        session.setBudget(checkpoint.getBudget());
        session.setPreferences(checkpoint.getPreferences());
        session.setHumanInputMode(checkpoint.getHumanInputMode());
        session.setStatus(checkpoint.getStatus());
        session.setCreatedAt(checkpoint.getCreateTime() != null ?
            checkpoint.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() :
            System.currentTimeMillis());
        return session;
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r");
    }

    private String toJson(ConversationMessage msg) {
        return String.format("{\"agentName\":\"%s\",\"content\":\"%s\",\"type\":\"%s\",\"timestamp\":\"%s\"}",
            msg.agentName(), escapeJson(msg.content()), msg.type(), msg.timestamp());
    }

    private String toJson(AutoGenOrchestrator.AutoGenPlanResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"finalPlan\":\"").append(escapeJson(result.finalPlan())).append("\",");
        sb.append("\"terminationReason\":\"").append(escapeJson(result.terminationReason())).append("\",");
        sb.append("\"totalRounds\":").append(result.totalRounds()).append(",");
        sb.append("\"durationMs\":").append(result.durationMs());
        sb.append("}");
        return sb.toString();
    }

    // ===== DTO 类 =====

    @Data
    public static class AutoGenStartRequest {
        private String destination;
        private Integer days;
        private String budget;
        private String preferences;
        private String humanInputMode; // ALWAYS, NEVER, TERMINATE
    }

    @Data
    public static class AutoGenStartResponse {
        private String sessionId;
        private String message;
        private ConversationSession session;

        public AutoGenStartResponse(String sessionId, String message, ConversationSession session) {
            this.sessionId = sessionId;
            this.message = message;
            this.session = session;
        }
    }

    @Data
    public static class AutoGenInputRequest {
        private String input;
    }

    @Data
    public static class AutoGenInputResponse {
        private boolean success;
        private String message;

        public AutoGenInputResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    @Data
    public static class AutoGenSessionResponse {
        private boolean success;
        private ConversationSession session;
        private String error;

        public AutoGenSessionResponse(boolean success, ConversationSession session, String error) {
            this.success = success;
            this.session = session;
            this.error = error;
        }
    }

    @Data
    public static class ConversationSession {
        private String sessionId;
        private String destination;
        private Integer days;
        private String budget;
        private String preferences;
        private String humanInputMode;
        private String status; // running, completed, error, terminated, timeout, paused, resuming
        private Long createdAt;
    }

    @Data
    public static class CheckpointResponse {
        private boolean success;
        private ConversationCheckpoint checkpoint;
        private String error;

        public CheckpointResponse(boolean success, ConversationCheckpoint checkpoint, String error) {
            this.success = success;
            this.checkpoint = checkpoint;
            this.error = error;
        }
    }

    @Data
    public static class CheckpointListResponse {
        private boolean success;
        private List<ConversationCheckpoint> checkpoints;
        private String error;

        public CheckpointListResponse(boolean success, List<ConversationCheckpoint> checkpoints, String error) {
            this.success = success;
            this.checkpoints = checkpoints;
            this.error = error;
        }
    }
}
