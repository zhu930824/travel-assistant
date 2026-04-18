package cn.sdh.travel.entity.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 规划记录实体类
 */
@Data
@TableName("plan_record")
public class PlanRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 目的地
     */
    private String destination;

    /**
     * 天数
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
     * 规划内容
     */
    private String planContent;

    /**
     * 状态：0-失败，1-成功
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
