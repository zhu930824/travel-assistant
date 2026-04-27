package cn.sdh.travel.entity.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对话Checkpoint实体类
 * 用于持久化存储对话状态，支持从任意checkpoint恢复对话
 */
@Data
@TableName("conversation_checkpoint")
public class ConversationCheckpoint {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话ID，唯一标识一次对话
     */
    private String sessionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 目的地
     */
    private String destination;

    /**
     * 游玩天数
     */
    private Integer days;

    /**
     * 预算
     */
    private String budget;

    /**
     * 偏好
     */
    private String preferences;

    /**
     * 人工输入模式: ALWAYS/NEVER/TERMINATE
     */
    private String humanInputMode;

    /**
     * 当前对话轮数
     */
    private Integer currentRound;

    /**
     * 最后发言者
     */
    private String lastSpeaker;

    /**
     * 是否已终止
     */
    private Boolean terminated;

    /**
     * 终止原因
     */
    private String terminationReason;

    /**
     * 消息列表JSON
     */
    private String messages;

    /**
     * 状态: active-活跃/paused-暂停/completed-完成
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
