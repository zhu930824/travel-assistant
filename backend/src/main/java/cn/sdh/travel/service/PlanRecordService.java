package cn.sdh.travel.service;

import cn.sdh.travel.entity.domain.PlanRecord;
import cn.sdh.travel.entity.dto.response.PlanRecordResponse;

import java.util.List;

/**
 * 规划记录服务接口
 */
public interface PlanRecordService {

    /**
     * 创建规划记录
     * @param userId 用户ID
     * @param destination 目的地
     * @param days 天数
     * @param budget 预算
     * @param preferences 偏好
     * @return 记录ID
     */
    Long createRecord(Long userId, String destination, Integer days, String budget, String preferences);

    /**
     * 更新规划结果
     * @param recordId 记录ID
     * @param planContent 规划内容
     * @param status 状态：1-成功，0-失败
     */
    void updateRecordResult(Long recordId, String planContent, Integer status);

    /**
     * 获取用户规划记录列表
     * @param userId 用户ID
     * @param limit 限制条数
     * @return 记录列表
     */
    List<PlanRecordResponse> getUserRecords(Long userId, int limit);

    /**
     * 获取用户本月规划次数
     * @param userId 用户ID
     * @return 次数
     */
    int getMonthlyCount(Long userId);

    /**
     * 根据ID获取记录
     * @param recordId 记录ID
     * @return 规划记录
     */
    PlanRecord getById(Long recordId);
}
