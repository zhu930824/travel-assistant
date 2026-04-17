package cn.sdh.travel.agent.tool;

import cn.sdh.travel.common.context.PlanContext;
import cn.sdh.travel.entity.dto.response.AgentOutputMessage;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.*;

/**
 * 自定义Agent执行器，支持流式输出
 * 将子Agent的流式输出实时转发给前端
 */
@Slf4j
public class AgentToolExecutor {

    private final ReactAgent agent;
    private final String sessionId;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Duration TIMEOUT = Duration.ofMinutes(5);

    public AgentToolExecutor(ReactAgent agent, String sessionId) {
        this.agent = agent;
        this.sessionId = sessionId;
    }

    /**
     * 流式执行子Agent，并将输出实时推送到前端
     */
    public AssistantMessage executeAgent(String input, ToolContext toolContext) {
        String actualInput = extractInputValue(input);
        log.info("【子Agent执行】开始执行 {} , 输入: {}", agent.name(), actualInput);

        // 构建消息列表
        List<Message> messagesToAdd = buildMessages(actualInput);

        // 获取编译后的Graph
        var graph = agent.getAndCompileGraph();

        // 流式调用子Agent
        Flux<NodeOutput> outputFlux = graph.stream(Map.of("messages", messagesToAdd));

        try {
            // 处理流式输出
            NodeOutput lastOutput = outputFlux
                .timeout(TIMEOUT)
                .map(nodeOutput -> {
                    processNodeOutput(nodeOutput);
                    return nodeOutput;
                })
                .blockLast();

            // 提取最终结果返回给监督者Agent
            return extractFinalResult(lastOutput);

        } catch (RuntimeException e) {
            if (e.getClass().getSimpleName().contains("Timeout")) {
                log.error("Agent execution timeout: {}", agent.name(), e);
                pushErrorToFrontend("执行超时，请稍后重试");
            } else {
                log.error("Agent execution error: {}", agent.name(), e);
                pushErrorToFrontend("执行出错：" + e.getMessage());
            }
            throw new RuntimeException("Agent execution failed", e);
        }
    }

    /**
     * 构建消息列表
     */
    private List<Message> buildMessages(String actualInput) {
        List<Message> messagesToAdd = new ArrayList<>();

        // 添加指令（如果有）
        String instruction = getInstructionFromAgent();
        if (instruction != null && !instruction.isEmpty()) {
            messagesToAdd.add(new UserMessage(instruction));
        }

        // 添加用户输入
        messagesToAdd.add(new UserMessage(actualInput));
        return messagesToAdd;
    }

    /**
     * 从Agent获取instruction（通过反射）
     */
    private String getInstructionFromAgent() {
        try {
            var field = ReactAgent.class.getDeclaredField("instruction");
            field.setAccessible(true);
            return (String) field.get(agent);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 处理节点输出
     */
    private void processNodeOutput(NodeOutput nodeOutput) {
        String node = nodeOutput.node();
        String agentName = agent.name();

        log.debug("【子Agent执行】收到节点输出 - node: {}, agent: {}", node, agentName);

        // 构建响应数据
        AgentOutputMessage.AgentOutputMessageBuilder messageBuilder = AgentOutputMessage.builder()
            .node(node)
            .agent(agentName)
            .stage(determineStage(agentName));

        if (nodeOutput instanceof StreamingOutput<?> streamingOutput) {
            processStreamingOutput(streamingOutput, messageBuilder);
        }
    }

    /**
     * 处理流式输出
     */
    private void processStreamingOutput(StreamingOutput<?> streamingOutput,
                                        AgentOutputMessage.AgentOutputMessageBuilder messageBuilder) {
        Message msg = streamingOutput.message();

        if (msg instanceof AssistantMessage assistantMessage) {
            if (assistantMessage.hasToolCalls()) {
                // 工具调用
                var toolCalls = assistantMessage.getToolCalls().get(0);
                AgentOutputMessage message = messageBuilder
                    .contentType("tool_execute")
                    .content("[工具执行]：" + toolCalls.name())
                    .hasContent(true)
                    .build();
                pushToFrontend(message);
            } else {
                // 流式消息内容
                String text = msg.getText();
                if (text != null && !text.trim().isEmpty()) {
                    AgentOutputMessage message = messageBuilder
                        .contentType("delta")
                        .content(text)
                        .hasContent(true)
                        .build();
                    pushToFrontend(message);
                }
            }
        } else if (msg instanceof ToolResponseMessage) {
            AgentOutputMessage message = messageBuilder
                .contentType("tool_complete")
                .content("[工具执行完成]")
                .hasContent(true)
                .build();
            pushToFrontend(message);
        }
    }

    /**
     * 推送消息到前端
     */
    private void pushToFrontend(AgentOutputMessage message) {
        if (!message.isHasContent()) {
            return;
        }

        // 更新会话状态中的累积内容
        PlanContext.SessionState state = PlanContext.getOrCreateSessionState(sessionId);
        if (message.getContentType().equals("delta")) {
            state.appendContent(message.getStage(), message.getContent());
        }

        // 获取SSE连接并推送
        var sink = PlanContext.getSessionEmitter(sessionId);
        if (sink != null) {
            String json = JSON.toJSONString(message);
            sink.next(ServerSentEvent.<String>builder()
                .event("message")
                .data(json)
                .build());
            log.debug("【SSE推送】已推送消息到前端: stage={}, content={}",
                message.getStage(), message.getContent().substring(0, Math.min(50, message.getContent().length())));
        } else {
            log.warn("【SSE推送】未找到sessionId对应的SSE连接: {}", sessionId);
        }
    }

    /**
     * 推送错误消息到前端
     */
    private void pushErrorToFrontend(String errorMessage) {
        var sink = PlanContext.getSessionEmitter(sessionId);
        if (sink != null) {
            Map<String, String> errorData = new HashMap<>();
            errorData.put("error", errorMessage);
            errorData.put("agent", agent.name());

            sink.next(ServerSentEvent.<String>builder()
                .event("error")
                .data(JSON.toJSONString(errorData))
                .build());
        }
    }

    /**
     * 提取最终结果
     */
    private AssistantMessage extractFinalResult(NodeOutput lastOutput) {
        if (lastOutput == null) {
            throw new RuntimeException("Agent execution returned no result");
        }

        OverAllState lastState = lastOutput.state();
        if (lastState == null) {
            throw new RuntimeException("Agent execution returned no state");
        }

        Optional<List> messages = lastState.value("messages", List.class);
        if (messages.isPresent()) {
            @SuppressWarnings("unchecked")
            List<Message> messageList = (List<Message>) messages.get();
            Message lastMessage = messageList.get(messageList.size() - 1);

            if (lastMessage instanceof AssistantMessage assistantMessage) {
                log.info("【子Agent执行】完成执行 {}, 返回结果长度: {}",
                    agent.name(), assistantMessage.getText().length());
                return assistantMessage;
            }
        }

        throw new RuntimeException("Failed to extract final result from agent execution");
    }

    /**
     * 提取输入值
     */
    private String extractInputValue(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        // 尝试解析JSON格式的输入
        if (input.startsWith("{")) {
            try {
                Map<String, Object> map = OBJECT_MAPPER.readValue(input, Map.class);
                Object inputValue = map.get("input");
                if (inputValue != null) {
                    return inputValue.toString();
                }
            } catch (Exception e) {
                log.debug("Failed to parse input as JSON, using raw input");
            }
        }

        return input;
    }

    /**
     * 根据Agent名称确定阶段标识
     */
    private String determineStage(String agentName) {
        if (agentName == null) {
            return "unknown";
        }
        if (agentName.contains("attraction")) {
            return "attraction";
        }
        if (agentName.contains("hotel")) {
            return "hotel";
        }
        if (agentName.contains("transport")) {
            return "transport";
        }
        if (agentName.contains("supervisor")) {
            return "supervisor";
        }
        return "unknown";
    }
}
