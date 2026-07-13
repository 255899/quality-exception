package com.gzklc.quality.entity;

import com.gzklc.quality.enums.DeptType;
import javax.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "t_department")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 部门名称 */
    private String name;

    /** 类型：课/组/产线 */
    @Enumerated(EnumType.STRING)
    private DeptType type;

    /** 上级ID（产线→组→课） */
    private Long parentId;
}
