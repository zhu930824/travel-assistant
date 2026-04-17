package cn.sdh.travel.agent.tool;


import com.alibaba.cloud.ai.graph.agent.MessageToolCallResultConverter;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.DefaultToolDefinition;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.tool.support.ToolDefinitions;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * 自定义AgentTool工厂类
 * 将ReactAgent作为Tool注入到监督者Agent中，支持流式输出
 */
public class AgentTool {

    private static final MessageToolCallResultConverter CONVERTER = new MessageToolCallResultConverter();



    /**
     * 创建AgentTool
     * @param agent 子Agent
     * @param sessionId 会话ID，用于SSE推送
     */
    public static ToolCallback create(ReactAgent agent, String sessionId) {
        try {
            // 获取Agent的输入Schema
            String originalSchema = getInputSchemaFromAgent(agent);

            // 获取执行方法
            Method method = AgentToolExecutor.class.getMethod(
                "executeAgent",
                String.class,
                ToolContext.class
            );

            // 构建工具定义
            DefaultToolDefinition.Builder builder = ToolDefinitions
                .builder(method)
                .name(agent.name())
                .description(agent.description());

            if (StringUtils.hasLength(originalSchema)) {
                String wrappedInputSchema = wrapSchemaInInputParameter(originalSchema);
                builder.inputSchema(wrappedInputSchema);
            }

            ToolDefinition toolDefinition = builder.build();
            AgentToolExecutor executor = new AgentToolExecutor(agent, sessionId);

            return MethodToolCallback.builder()
                .toolDefinition(toolDefinition)
                .toolMethod(method)
                .toolObject(executor)
                .toolCallResultConverter(CONVERTER)
                .build();

        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to create agent tool", e);
        }
    }

    /**
     * 从Agent获取inputSchema（通过反射）
     */
    private static String getInputSchemaFromAgent(ReactAgent agent) {
        try {
            var field = ReactAgent.class.getDeclaredField("inputSchema");
            field.setAccessible(true);
            return (String) field.get(agent);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 将原始schema包装为input参数格式
     */
    private static String wrapSchemaInInputParameter(String originalSchema) {
        return String.format("""
            {
              "type": "object",
              "properties": {
                "input": %s
              },
              "required": ["input"]
            }
            """, originalSchema);
    }
}
