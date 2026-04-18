package cn.sdh.travel.mapper;

import cn.sdh.travel.entity.domain.PlanRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

/**
 * 规划记录Mapper
 */
@Mapper
public interface PlanRecordMapper extends BaseMapper<PlanRecord> {

    /**
     * 统计用户本月规划次数
     */
    @Select("SELECT COUNT(*) FROM plan_record WHERE user_id = #{userId} AND create_time >= #{startTime} AND create_time < #{endTime}")
    int countMonthlyPlans(Long userId, LocalDateTime startTime, LocalDateTime endTime);
}