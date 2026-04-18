package cn.sdh.travel.entity.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 旅游规划请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelPlanRequest {

    /**
     * 目的地
     */
    @NotBlank(message = "目的地不能为空")
    private String destination;

    /**
     * 天数
     */
    @Min(value = 1, message = "天数最少为1天")
    @Max(value = 30, message = "天数最多为30天")
    private Integer days = 3;

    /**
     * 预算
     */
    private String budget;

    /**
     * 偏好列表（如：美食、历史文化、自然风光等）
     */
    private List<String> preferences;

    /**
     * 获取偏好字符串（逗号分隔）
     */
    public String getPreferencesString() {
        if (preferences == null || preferences.isEmpty()) {
            return null;
        }
        return String.join("、", preferences);
    }
}
