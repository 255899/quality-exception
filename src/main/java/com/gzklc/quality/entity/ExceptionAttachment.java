package com.gzklc.quality.entity;

import javax.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 异常报告附件（图片）
 */
@Data
@Entity
@Table(name = "t_exception_attachment")
public class ExceptionAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属异常报告 */
    private Long exceptionId;

    /** 原始文件名 */
    @Column(length = 255)
    private String fileName;

    /** 存储路径（相对 uploads/） */
    @Column(length = 255)
    private String filePath;

    /** 访问 URL */
    @Column(length = 255)
    private String url;

    /** 文件大小（字节） */
    private Long size;

    /** mime */
    @Column(length = 64)
    private String contentType;

    private LocalDateTime uploadedAt;
}
