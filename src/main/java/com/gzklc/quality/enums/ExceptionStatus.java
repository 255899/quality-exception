package com.gzklc.quality.enums;

import lombok.Getter;

/**
 * 异常报告状态
 */
@Getter
public enum ExceptionStatus {
    DRAFT("草稿"),
    APPLIED("已申请"),
    QC_HANDLED("品管已处理"),
    SHIFT_CONFIRMED("班长已首件确认"),
    QC_CONFIRMED("品管已首件确认"),
    LEADER_CONFIRMED("组长已确认"),
    CHIEF_CONFIRMED("课长已确认"),
    CLOSED("已关闭");

    private final String desc;

    ExceptionStatus(String desc) {
        this.desc = desc;
    }
}
