package com.gzklc.quality.repository;

import com.gzklc.quality.entity.Part;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartRepository extends JpaRepository<Part, Long> {
    List<Part> findByProcessIdOrderByCodeAsc(Long processId);
}
