package com.gzklc.quality.entity;

import javax.persistence.*;
import lombok.Data;

/**
 * 部品（每道工序对应的可生产零件）
 */
@Data
@Entity
@Table(name = "t_part")
public class Part {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 部品编号，如 3510120EH1 */
    @Column(length = 64)
    private String code;

    /** 部品名称，如 E38B前制动盘 */
    @Column(length = 128)
    private String name;

    /** 所属工序ID */
    private Long processId;
}
