package cn.sdh.travel.agent.autogen;

/**
 * 人类输入模式
 * 控制 UserProxyAgent 何时请求用户输入
 */
public enum HumanInputMode {

    /**
     * 每轮都请求用户输入（完全人工参与）
     */
    ALWAYS,

    /**
     * 从不请求用户输入（完全自动）
     */
    NEVER,

    /**
     * 仅在检测到终止条件时请求用户确认
     */
    TERMINATE
}
