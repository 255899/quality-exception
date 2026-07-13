package com.gzklc.quality.entity;

import com.gzklc.quality.enums.ExceptionStatus;
import javax.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_exception_report")
public class ExceptionReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 异常单编号（提交后由系统生成） */
    @Column(unique = true, length = 32)
    private String code;

    /** 现场实施者 */
    private String implementer;

    /** 发生时间 */
    private LocalDateTime occurredAt;

    /** 班次：早班/中班/晚班 */
    private String shift;

    /** 课ID */
    private Long sectionId;

    /** 组ID */
    private Long groupId;

    /** 产线ID */
    private Long lineId;

    /** 工序ID */
    private Long processId;

    /** 部品ID（选填） */
    private Long partId;

    /** 异常描述 */
    @Column(length = 1000)
    private String description;

    /** 发现动机（选填） */
    @Column(length = 500)
    private String discoveryReason;

    /** 异常发生选项（1-9 选 1） */
    @Column(length = 32)
    private String reasonType;

    /** 指派品管人员 ID */
    private Long qcHandlerId;

    /** 作业/报告者 */
    private String reporter;

    /** 当前状态 */
    @Enumerated(EnumType.STRING)
    private ExceptionStatus status;

    /** 申请人ID */
    private Long applicantId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
