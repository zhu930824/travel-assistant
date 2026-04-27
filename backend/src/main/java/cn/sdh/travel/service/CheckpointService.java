package cn.sdh.travel.service;

import cn.sdh.travel.entity.domain.ConversationCheckpoint;
import cn.sdh.travel.agent.autogen.ConversationState;

import java.util.List;

/**
 * 对话Checkpoint服务接口
 * 提供对话状态的持久化和恢复能力
 */
public interface CheckpointService {

    /**
     * 创建新的checkpoint
     *
     * @param userId         用户ID
     * @param destination    目的地
     * @param days           天数
     * @param budget         预算
     * @param preferences    偏好
     * @param humanInputMode 人工输入模式
     * @return 会话ID
     */
    String createCheckpoint(Long userId, String destination, int days,
                            String budget, String preferences, String humanInputMode);

    /**
     * 保存对话状态到checkpoint
     *
     * @param sessionId 会话ID
     * @param state     对话状态
     */
    void saveState(String sessionId, ConversationState state);

    /**
     * 加载对话状态
     *
     * @param sessionId 会话ID
     * @return 对话状态，如果不存在返回null
     */
    ConversationState loadState(String sessionId);

    /**
     * 暂停对话（等待人工输入时调用）
     *
     * @param sessionId 会话ID
     */
    void pauseSession(String sessionId);

    /**
     * 恢复对话
     *
     * @param sessionId 会话ID
     */
    void resumeSession(String sessionId);

    /**
     * 完成对话
     *
     * @param sessionId         会话ID
     * @param terminationReason 终止原因
     */
    void completeSession(String sessionId, String terminationReason);

    /**
     * 获取checkpoint详情
     *
     * @param sessionId 会话ID
     * @return checkpoint详情
     */
    ConversationCheckpoint getCheckpoint(String sessionId);

    /**
     * 获取用户的活跃checkpoint列表
     *
     * @param userId 用户ID
     * @return checkpoint列表
     */
    List<ConversationCheckpoint> getUserActiveCheckpoints(Long userId);

    /**
     * 删除checkpoint
     *
     * @param sessionId 会话ID
     * @return 是否删除成功
     */
    boolean deleteCheckpoint(String sessionId);
}
