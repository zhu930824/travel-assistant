package cn.sdh.travel.service.impl;

import cn.sdh.travel.entity.domain.PlanRecord;
import cn.sdh.travel.entity.dto.response.PlanRecordResponse;
import cn.sdh.travel.mapper.PlanRecordMapper;
import cn.sdh.travel.service.PlanRecordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 规划记录服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlanRecordServiceImpl implements PlanRecordService {

    private final PlanRecordMapper planRecordMapper;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    @Transactional
    public Long createRecord(Long userId, String destination, Integer days, String budget, String preferences) {
        PlanRecord record = new PlanRecord();
        record.setUserId(userId);
        record.setDestination(destination);
        record.setDays(days);
        record.setBudget(budget);
        record.setPreferences(preferences);
        record.setStatus(0); // 默认为进行中
        record.setCreateTime(LocalDateTime.now());

        planRecordMapper.insert(record);
        log.info("创建规划记录: recordId={}, userId={}, destination={}", record.getId(), userId, destination);

        return record.getId();
    }

    @Override
    @Transactional
    public void updateRecordResult(Long recordId, String planContent, Integer status) {
        PlanRecord record = planRecordMapper.selectById(recordId);
        if (record != null) {
            record.setPlanContent(planContent);
            record.setStatus(status);
            planRecordMapper.updateById(record);
            log.info("更新规划记录: recordId={}, status={}", recordId, status);
        }
    }

    @Override
    public List<PlanRecordResponse> getUserRecords(Long userId, int limit) {
        LambdaQueryWrapper<PlanRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlanRecord::getUserId, userId)
               .orderByDesc(PlanRecord::getCreateTime)
               .last("LIMIT " + limit);

        List<PlanRecord> records = planRecordMapper.selectList(wrapper);

        return records.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public int getMonthlyCount(Long userId) {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startTime = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endTime = currentMonth.plusMonths(1).atDay(1).atStartOfDay();
        return planRecordMapper.countMonthlyPlans(userId, startTime, endTime);
    }

    @Override
    public PlanRecord getById(Long recordId) {
        return planRecordMapper.selectById(recordId);
    }

    @Override
    @Transactional
    public void updatePlanData(Long id, String planData) {
        log.info("更新结构化行程数据: recordId={}", id);
        int rows = planRecordMapper.updatePlanData(id, planData);
        if (rows == 0) {
            log.warn("更新结构化行程数据失败，记录不存在: recordId={}", id);
        }
    }

    @Override
    public String getPlanData(Long id) {
        return planRecordMapper.getPlanData(id);
    }

    /**
     * 转换为响应对象
     */
    private PlanRecordResponse convertToResponse(PlanRecord record) {
        String preferencesStr = record.getPreferences();
        // 如果偏好是列表格式，转换为字符串
        if (preferencesStr != null && preferencesStr.startsWith("[") && preferencesStr.endsWith("]")) {
            preferencesStr = preferencesStr.substring(1, preferencesStr.length() - 1)
                                          .replace("\"", "")
                                          .replace(",", "、");
        }

        return PlanRecordResponse.builder()
                .id(record.getId())
                .destination(record.getDestination())
                .days(record.getDays())
                .budget(record.getBudget())
                .preferences(preferencesStr)
                .planContent(record.getPlanContent())
                .status(record.getStatus())
                .createTime(record.getCreateTime())
                .dateStr(record.getCreateTime() != null ? record.getCreateTime().format(DATE_FORMATTER) : "")
                .build();
    }
}
