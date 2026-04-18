package cn.sdh.travel.entity.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 会员升级请求
 */
@Data
public class UpgradeRequest {

    /**
     * 会员等级：PRO-专业版, ENTERPRISE-企业版
     */
    @NotBlank(message = "会员等级不能为空")
    @Pattern(regexp = "^(PRO|ENTERPRISE)$", message = "会员等级不正确")
    private String memberLevel;

    /**
     * 购买月数（1-12个月）
     */
    @Min(value = 1, message = "购买月数至少为1个月")
    private Integer months = 1;
}
