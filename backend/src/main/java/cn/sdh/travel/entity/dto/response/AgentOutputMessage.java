package cn.sdh.travel.entity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent输出消息 - SSE数据格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentOutputMessage {

    /**
     * 当前执行的节点名称
     */
    private String node;

    /**
     * 当前执行的Agent名称
     */
    private String agent;

    /**
     * 阶段标识：attraction/hotel/transport/supervisor
     */
    private String stage;

    /**
     * 内容类型：delta/tool_execute/tool_complete
     */
    private String contentType;

    /**
     * 具体内容
     */
    private String content;

    /**
     * 是否包含有效内容
     */
    private boolean hasContent;

    /**
     * 时间戳
     */
    private long timestamp;

    public AgentOutputMessage(String node, String agent, String stage, String contentType, String content, boolean hasContent) {
        this.node = node;
        this.agent = agent;
        this.stage = stage;
        this.contentType = contentType;
        this.content = content;
        this.hasContent = hasContent;
        this.timestamp = System.currentTimeMillis();
    }
}
