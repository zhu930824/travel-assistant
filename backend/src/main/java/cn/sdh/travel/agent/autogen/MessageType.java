package cn.sdh.travel.agent.autogen;

/**
 * 对话消息类型
 */
public enum MessageType {

    /**
     * 普通文本消息
     */
    TEXT,

    /**
     * 工具调用请求
     */
    TOOL_CALL,

    /**
     * 工具调用结果
     */
    TOOL_RESULT,

    /**
     * 人类输入
     */
    HUMAN_INPUT,

    /**
     * 系统消息
     */
    SYSTEM,

    /**
     * 终止信号
     */
    TERMINATE
}
