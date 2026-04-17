package cn.sdh.travel.common.context;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.FluxSink;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE连接管理器
 * 管理sessionId到FluxSink的映射，支持子Agent获取当前会话的SSE连接
 */
public class PlanContext {

    /**
     * 线程本地存储当前线程的FluxSink
     */
    private static final InheritableThreadLocal<FluxSink<ServerSentEvent<String>>> context =
        new InheritableThreadLocal<>();

    /**
     * 会话级别的SSE连接映射
     */
    private static final Map<String, FluxSink<ServerSentEvent<String>>> sessionEmitter =
        new ConcurrentHashMap<>();

    /**
     * 会话状态存储（用于存储累积的内容）
     */
    private static final Map<String, SessionState> sessionStates = new ConcurrentHashMap<>();

    /**
     * 设置当前线程的FluxSink
     */
    public static void set(FluxSink<ServerSentEvent<String>> sink) {
        context.set(sink);
    }

    /**
     * 获取当前线程的FluxSink
     */
    public static FluxSink<ServerSentEvent<String>> get() {
        return context.get();
    }

    /**
     * 设置会话的SSE连接
     */
    public static void setSessionEmitter(String sessionId, FluxSink<ServerSentEvent<String>> sink) {
        sessionEmitter.put(sessionId, sink);
    }

    /**
     * 获取会话的SSE连接
     */
    public static FluxSink<ServerSentEvent<String>> getSessionEmitter(String sessionId) {
        return sessionEmitter.get(sessionId);
    }

    /**
     * 移除会话的SSE连接
     */
    public static void removeSessionEmitter(String sessionId) {
        sessionEmitter.remove(sessionId);
        sessionStates.remove(sessionId);
    }

    /**
     * 清除当前线程的FluxSink
     */
    public static void remove() {
        context.remove();
    }

    /**
     * 获取或创建会话状态
     */
    public static SessionState getOrCreateSessionState(String sessionId) {
        return sessionStates.computeIfAbsent(sessionId, k -> new SessionState());
    }

    /**
     * 会话状态内部类 - 存储各Agent累积的内容
     */
    public static class SessionState {
        private final StringBuilder attractionContent = new StringBuilder();
        private final StringBuilder hotelContent = new StringBuilder();
        private final StringBuilder transportContent = new StringBuilder();
        private final StringBuilder supervisorContent = new StringBuilder();

        public StringBuilder getAttractionContent() {
            return attractionContent;
        }

        public StringBuilder getHotelContent() {
            return hotelContent;
        }

        public StringBuilder getTransportContent() {
            return transportContent;
        }

        public StringBuilder getSupervisorContent() {
            return supervisorContent;
        }

        public void appendContent(String stage, String content) {
            switch (stage) {
                case "attraction" -> attractionContent.append(content);
                case "hotel" -> hotelContent.append(content);
                case "transport" -> transportContent.append(content);
                case "supervisor" -> supervisorContent.append(content);
            }
        }

        public String getContent(String stage) {
            return switch (stage) {
                case "attraction" -> attractionContent.toString();
                case "hotel" -> hotelContent.toString();
                case "transport" -> transportContent.toString();
                case "supervisor" -> supervisorContent.toString();
                default -> "";
            };
        }
    }
}
