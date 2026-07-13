package com.gzklc.quality.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FlowActionRequest {
    @NotNull
    private Long exceptionId;

    /** 处理人ID */
    @NotNull
    private Long handlerId;

    /** 通过 / 驳回 */
    @NotBlank
    private String action;

    private String comment;
}
