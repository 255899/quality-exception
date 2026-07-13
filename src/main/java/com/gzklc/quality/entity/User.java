package com.gzklc.quality.entity;

import javax.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "t_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 登录名 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 角色: SHIFT_LEADER / QC / SECTION_LEADER / SECTION_CHIEF / QUALITY_CHIEF */
    private String role;

    /** 所属部门ID */
    private Long deptId;

    /** 登录密码（明文，演示用；生产请用 BCrypt） */
    @Column(length = 64)
    private String password;

    /** 是否启用 */
    private Boolean enabled = true;
}
