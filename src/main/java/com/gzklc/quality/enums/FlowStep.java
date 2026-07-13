package com.gzklc.quality.enums;

import lombok.Getter;

/**
 * 流程节点
 */
@Getter
public enum FlowStep {
    APPLY("异常申请", "SHIFT_LEADER"),
    QC_HANDLE("品管异常处理", "QC"),
    SHIFT_CONFIRM("恢复生产首件确认(现场班长)", "SHIFT_LEADER"),
    QC_CONFIRM("恢复生产首件确认(现场品管)", "QC"),
    LEADER_CONFIRM("加工课组长确认", "SECTION_LEADER"),
    CHIEF_CONFIRM("加工课课长确认", "SECTION_CHIEF"),
    QUALITY_CONFIRM("品质课课长确认", "QUALITY_CHIEF");

    private final String desc;
    private final String requiredRole;

    FlowStep(String desc, String requiredRole) {
        this.desc = desc;
        this.requiredRole = requiredRole;
    }
}
