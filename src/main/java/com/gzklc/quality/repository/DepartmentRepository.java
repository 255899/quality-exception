package com.gzklc.quality.repository;

import com.gzklc.quality.entity.Department;
import com.gzklc.quality.enums.DeptType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByType(DeptType type);
    List<Department> findByParentId(Long parentId);
}
