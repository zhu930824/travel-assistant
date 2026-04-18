package cn.sdh.travel.common.enums;

import lombok.Getter;

/**
 * 会员等级枚举
 */
@Getter
public enum MemberLevel {

    FREE("FREE", "免费版", 5, "每月5次规划"),
    PRO("PRO", "专业版", Integer.MAX_VALUE, "无限规划次数"),
    ENTERPRISE("ENTERPRISE", "企业版", Integer.MAX_VALUE, "无限规划次数");

    private final String code;
    private final String name;
    private final int monthlyLimit;
    private final String description;

    MemberLevel(String code, String name, int monthlyLimit, String description) {
        this.code = code;
        this.name = name;
        this.monthlyLimit = monthlyLimit;
        this.description = description;
    }

    public static MemberLevel fromCode(String code) {
        if (code == null) {
            return FREE;
        }
        for (MemberLevel level : values()) {
            if (level.getCode().equals(code)) {
                return level;
            }
        }
        return FREE;
    }
}
