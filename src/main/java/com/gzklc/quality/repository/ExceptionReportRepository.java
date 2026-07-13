package com.gzklc.quality.repository;

import com.gzklc.quality.entity.ExceptionReport;
import com.gzklc.quality.enums.ExceptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ExceptionReportRepository extends JpaRepository<ExceptionReport, Long> {
    List<ExceptionReport> findByStatusOrderByCreatedAtDesc(ExceptionStatus status);
    List<ExceptionReport> findAllByOrderByCreatedAtDesc();

    /** 按课ID集合查询（用于课长/组长的数据范围） */
    List<ExceptionReport> findBySectionIdInOrderByCreatedAtDesc(Collection<Long> sectionIds);

    /** 按组ID集合查询（用于班长的数据范围） */
    List<ExceptionReport> findByGroupIdInOrderByCreatedAtDesc(Collection<Long> groupIds);

    /** 按状态+课ID集合 */
    List<ExceptionReport> findByStatusAndSectionIdInOrderByCreatedAtDesc(ExceptionStatus status, Collection<Long> sectionIds);

    List<ExceptionReport> findByStatusAndGroupIdInOrderByCreatedAtDesc(ExceptionStatus status, Collection<Long> groupIds);

    /** 统计：按状态分组计数（按数据范围过滤） */
    @Query("SELECT r.status AS status, COUNT(r) AS cnt FROM ExceptionReport r " +
            "WHERE (:sectionIds IS NULL OR r.sectionId IN :sectionIds) " +
            "AND (:groupIds IS NULL OR r.groupId IN :groupIds) " +
            "AND (:from IS NULL OR r.createdAt >= :from) " +
            "AND (:to IS NULL OR r.createdAt < :to) " +
            "GROUP BY r.status")
    List<Object[]> countByStatusInScope(@Param("sectionIds") Collection<Long> sectionIds,
                                        @Param("groupIds") Collection<Long> groupIds,
                                        @Param("from") LocalDateTime from,
                                        @Param("to") LocalDateTime to);

    /** 统计：按课分组计数 */
    @Query("SELECT r.sectionId AS sectionId, COUNT(r) AS cnt FROM ExceptionReport r " +
            "WHERE (:sectionIds IS NULL OR r.sectionId IN :sectionIds) " +
            "AND (:groupIds IS NULL OR r.groupId IN :groupIds) " +
            "AND (:from IS NULL OR r.createdAt >= :from) " +
            "AND (:to IS NULL OR r.createdAt < :to) " +
            "GROUP BY r.sectionId")
    List<Object[]> countBySectionInScope(@Param("sectionIds") Collection<Long> sectionIds,
                                         @Param("groupIds") Collection<Long> groupIds,
                                         @Param("from") LocalDateTime from,
                                         @Param("to") LocalDateTime to);
}
