package com.gzklc.quality.entity;

import javax.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "t_process")
public class Process {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 工序名称 */
    private String name;

    /** 所属产线ID */
    private Long lineId;
}
