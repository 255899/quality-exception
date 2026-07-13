package com.gzklc.quality.entity;

import com.gzklc.quality.enums.FlowStep;
import javax.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_process_flow")
public class ProcessFlow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联异常单ID */
    private Long exceptionId;

    /** 流程节点 */
    @Enumerated(EnumType.STRING)
    private FlowStep step;

    /** 处理人ID */
    private Long handlerId;

    /** 处理人姓名 */
    private String handlerName;

    /** 处理动作：通过 / 驳回 / 备注 */
    private String action;

    /** 处理意见 */
    @Column(length = 1000)
    private String comment;

    @CreationTimestamp
    private LocalDateTime processedAt;
}
