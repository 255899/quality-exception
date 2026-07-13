package com.gzklc.quality.repository;

import com.gzklc.quality.entity.ProcessFlow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessFlowRepository extends JpaRepository<ProcessFlow, Long> {
    List<ProcessFlow> findByExceptionIdOrderByProcessedAtAsc(Long exceptionId);
}
