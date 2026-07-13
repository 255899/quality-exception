package com.gzklc.quality.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ApplyRequest {
    private String implementer;
    private LocalDateTime occurredAt;
    private String shift;

    @NotNull
    private Long sectionId;

    @NotNull
    private Long groupId;

    @NotNull
    private Long lineId;

    private Long processId;

    /** 部品ID（选填，需先选工序） */
    private Long partId;

    @NotBlank
    private String description;

    /** 发现动机（选填） */
    private String discoveryReason;

    /** 异常发生选项（1-9） */
    private String reasonType;

    /** 指派品管人员 ID */
    private Long qcHandlerId;

    /** 附件 ID 列表（由上传接口返回） */
    private List<Long> attachmentIds;

    @NotBlank
    private String reporter;

    /** 申请人ID（简化：实际项目用 session/token） */
    private Long applicantId;
}
