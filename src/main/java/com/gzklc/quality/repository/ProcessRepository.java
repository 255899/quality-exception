package com.gzklc.quality.repository;

import com.gzklc.quality.entity.Process;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessRepository extends JpaRepository<Process, Long> {
    List<Process> findByLineId(Long lineId);
}
