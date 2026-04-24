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

    private static final InheritableThreadLocal<FluxSink<ServerSentEvent<String>>> context =
        new InheritableThreadLocal<>();

    private static final Map<String, FluxSink<ServerSentEvent<String>>> sessionEmitter =
        new ConcurrentHashMap<>();

    private static final Map<String, SessionState> sessionStates = new ConcurrentHashMap<>();

    public static void set(FluxSink<ServerSentEvent<String>> sink) {
        context.set(sink);
    }

    public static FluxSink<ServerSentEvent<String>> get() {
        return context.get();
    }

    public static void setSessionEmitter(String sessionId, FluxSink<ServerSentEvent<String>> sink) {
        sessionEmitter.put(sessionId, sink);
    }

    public static FluxSink<ServerSentEvent<String>> getSessionEmitter(String sessionId) {
        return sessionEmitter.get(sessionId);
    }

    public static void removeSessionEmitter(String sessionId) {
        sessionEmitter.remove(sessionId);
        sessionStates.remove(sessionId);
    }

    public static void remove() {
        context.remove();
    }

    public static SessionState getOrCreateSessionState(String sessionId) {
        return sessionStates.computeIfAbsent(sessionId, k -> new SessionState());
    }

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
