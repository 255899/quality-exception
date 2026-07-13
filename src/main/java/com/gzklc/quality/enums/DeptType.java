package com.gzklc.quality.enums;

import lombok.Getter;

/**
 * 部门类型
 */
@Getter
public enum DeptType {
    SECTION("课"),
    GROUP("组"),
    LINE("产线");

    private final String desc;

    DeptType(String desc) {
        this.desc = desc;
    }
}
