package com.gzklc.quality.repository;

import com.gzklc.quality.entity.ExceptionAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExceptionAttachmentRepository extends JpaRepository<ExceptionAttachment, Long> {
    List<ExceptionAttachment> findByExceptionIdOrderByIdAsc(Long exceptionId);
}
