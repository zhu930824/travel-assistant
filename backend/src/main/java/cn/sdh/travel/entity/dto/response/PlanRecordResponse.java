package cn.sdh.travel.entity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 规划记录响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanRecordResponse {

    /**
     * 记录ID
     */
    private Long id;

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

    /**
     * 格式化日期字符串
     */
    private String dateStr;
}
