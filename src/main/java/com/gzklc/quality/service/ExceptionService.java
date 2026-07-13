package com.gzklc.quality.service;

import com.gzklc.quality.dto.ApplyRequest;
import com.gzklc.quality.dto.ExceptionDetailDTO;
import com.gzklc.quality.dto.FlowActionRequest;
import com.gzklc.quality.entity.Department;
import com.gzklc.quality.entity.ExceptionAttachment;
import com.gzklc.quality.entity.ExceptionReport;
import com.gzklc.quality.entity.Process;
import com.gzklc.quality.entity.ProcessFlow;
import com.gzklc.quality.entity.User;
import com.gzklc.quality.enums.DeptType;
import com.gzklc.quality.enums.ExceptionStatus;
import com.gzklc.quality.enums.FlowStep;
import com.gzklc.quality.repository.DepartmentRepository;
import com.gzklc.quality.repository.ExceptionAttachmentRepository;
import com.gzklc.quality.repository.ExceptionReportRepository;
import com.gzklc.quality.repository.ProcessFlowRepository;
import com.gzklc.quality.repository.ProcessRepository;
import com.gzklc.quality.repository.UserRepository;
import com.gzklc.quality.security.AuthContext;
import com.gzklc.quality.security.DataScope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExceptionService {

    private final ExceptionReportRepository reportRepo;
    private final ProcessFlowRepository flowRepo;
    private final UserRepository userRepo;
    private final DepartmentRepository deptRepo;
    private final ProcessRepository processRepo;
    private final ExceptionAttachmentRepository attachmentRepo;
    private final DataScope dataScope;

    /**
     * 预览异常单编号：FM{yy}-{MM}-{seq}
     */
    public String previewCode() {
        LocalDate today = LocalDate.now();
        String yy = String.format("%02d", today.getYear() % 100);
        String mm = String.format("%02d", today.getMonthValue());
        long count = reportRepo.count() + 1;
        return String.format("FM%s-%s-%03d", yy, mm, count);
    }

    /**
     * 提交申请
     */
    @Transactional
    public ExceptionReport apply(ApplyRequest req) {
        // 若请求未带 applicantId 但有登录态，则用登录用户
        if (req.getApplicantId() == null) {
            User u = AuthContext.get();
            if (u != null) req.setApplicantId(u.getId());
        }
        ExceptionReport report = new ExceptionReport();
        report.setCode(generateFinalCode());
        report.setImplementer(req.getImplementer());
        report.setOccurredAt(req.getOccurredAt());
        report.setShift(req.getShift());
        report.setSectionId(req.getSectionId());
        report.setGroupId(req.getGroupId());
        report.setLineId(req.getLineId());
        report.setProcessId(req.getProcessId());
        report.setPartId(req.getPartId());
        report.setDescription(req.getDescription());
        report.setDiscoveryReason(req.getDiscoveryReason());
        report.setReasonType(req.getReasonType());
        report.setQcHandlerId(req.getQcHandlerId());
        report.setReporter(req.getReporter());
        report.setApplicantId(req.getApplicantId());
        report.setStatus(ExceptionStatus.APPLIED);
        report.setUpdatedAt(LocalDateTime.now());
        report = reportRepo.save(report);
        final Long savedReportId = report.getId();

        // 关联附件
        if (req.getAttachmentIds() != null) {
            for (Long attId : req.getAttachmentIds()) {
                final Long fid = attId;
                attachmentRepo.findById(fid).ifPresent(a -> {
                    a.setExceptionId(savedReportId);
                    attachmentRepo.save(a);
                });
            }
        }

        // 写入流程节点：申请
        saveFlow(report.getId(), FlowStep.APPLY, req.getApplicantId(),
                resolveName(req.getApplicantId()), "提交", "班长提交异常申请");

        return report;
    }

    private String generateFinalCode() {
        LocalDate today = LocalDate.now();
        String yy = String.format("%02d", today.getYear() % 100);
        String mm = String.format("%02d", today.getMonthValue());
        long count = reportRepo.count() + 1;
        return String.format("FM%s-%s-%03d", yy, mm, count);
    }

    private String resolveName(Long userId) {
        if (userId == null) return "系统";
        return userRepo.findById(userId).map(User::getRealName).orElse("未知");
    }

    private void saveFlow(Long exceptionId, FlowStep step, Long handlerId, String handlerName, String action, String comment) {
        ProcessFlow flow = new ProcessFlow();
        flow.setExceptionId(exceptionId);
        flow.setStep(step);
        flow.setHandlerId(handlerId);
        flow.setHandlerName(handlerName);
        flow.setAction(action);
        flow.setComment(comment);
        flowRepo.save(flow);
    }

    /**
     * 通用流程处理
     */
    @Transactional
    public ExceptionReport process(FlowStep step, FlowActionRequest req) {
        ExceptionReport report = reportRepo.findById(req.getExceptionId())
                .orElseThrow(() -> new IllegalArgumentException("异常单不存在"));

        // 校验当前状态是否允许该步骤
        ExceptionStatus expected = expectedStatusFor(step);
        if (report.getStatus() != expected) {
            throw new IllegalStateException("当前状态 [" + report.getStatus().getDesc() + "] 不允许执行 ["
                    + step.getDesc() + "]，期望状态: " + expected.getDesc());
        }

        // 写入流程
        saveFlow(report.getId(), step, req.getHandlerId(), resolveName(req.getHandlerId()),
                req.getAction(), req.getComment());

        // 状态推进
        ExceptionStatus next = nextStatus(step, req.getAction());
        report.setStatus(next);
        report.setUpdatedAt(LocalDateTime.now());
        return reportRepo.save(report);
    }

    private ExceptionStatus expectedStatusFor(FlowStep step) {
        if (step == FlowStep.QC_HANDLE) return ExceptionStatus.APPLIED;
        if (step == FlowStep.SHIFT_CONFIRM) return ExceptionStatus.QC_HANDLED;
        if (step == FlowStep.QC_CONFIRM) return ExceptionStatus.SHIFT_CONFIRMED;
        if (step == FlowStep.LEADER_CONFIRM) return ExceptionStatus.QC_CONFIRMED;
        if (step == FlowStep.CHIEF_CONFIRM) return ExceptionStatus.LEADER_CONFIRMED;
        if (step == FlowStep.QUALITY_CONFIRM) return ExceptionStatus.CHIEF_CONFIRMED;
        throw new IllegalArgumentException("未支持的步骤: " + step);
    }

    private ExceptionStatus nextStatus(FlowStep step, String action) {
        boolean passed = "通过".equals(action) || "PASS".equalsIgnoreCase(action) || "APPROVE".equalsIgnoreCase(action);
        if (!passed) {
            if (step == FlowStep.QC_HANDLE) return ExceptionStatus.APPLIED;
            if (step == FlowStep.SHIFT_CONFIRM) return ExceptionStatus.QC_HANDLED;
            if (step == FlowStep.QC_CONFIRM) return ExceptionStatus.SHIFT_CONFIRMED;
            if (step == FlowStep.LEADER_CONFIRM) return ExceptionStatus.QC_CONFIRMED;
            if (step == FlowStep.CHIEF_CONFIRM) return ExceptionStatus.LEADER_CONFIRMED;
            if (step == FlowStep.QUALITY_CONFIRM) return ExceptionStatus.CHIEF_CONFIRMED;
            throw new IllegalArgumentException("未支持的步骤: " + step);
        }
        if (step == FlowStep.QC_HANDLE) return ExceptionStatus.QC_HANDLED;
        if (step == FlowStep.SHIFT_CONFIRM) return ExceptionStatus.SHIFT_CONFIRMED;
        if (step == FlowStep.QC_CONFIRM) return ExceptionStatus.QC_CONFIRMED;
        if (step == FlowStep.LEADER_CONFIRM) return ExceptionStatus.LEADER_CONFIRMED;
        if (step == FlowStep.CHIEF_CONFIRM) return ExceptionStatus.CHIEF_CONFIRMED;
        if (step == FlowStep.QUALITY_CONFIRM) return ExceptionStatus.CLOSED;
        throw new IllegalArgumentException("未支持的步骤: " + step);
    }

    public ExceptionDetailDTO detail(Long id) {
        ExceptionReport report = reportRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("异常单不存在"));
        // 访问控制：仅可查看数据范围内的异常单
        User u = AuthContext.get();
        if (u != null && !dataScope.canViewReport(u, report.getSectionId(), report.getGroupId())) {
            throw new SecurityException("无权访问此异常单");
        }
        List<ProcessFlow> flows = flowRepo.findByExceptionIdOrderByProcessedAtAsc(id);
        User applicant = report.getApplicantId() == null ? null
                : userRepo.findById(report.getApplicantId()).orElse(null);
        ExceptionDetailDTO dto = new ExceptionDetailDTO();
        dto.setReport(report);
        dto.setFlows(flows);
        dto.setApplicant(applicant);
        return dto;
    }

    /**
     * 按当前用户的数据范围列出异常单（可按状态过滤）
     */
    public List<ExceptionReport> listInScope(ExceptionStatus status) {
        DataScope.Scope s = dataScope.currentScope();
        boolean sectionRestricted = !s.sectionIds.isEmpty();
        boolean groupRestricted = !s.groupIds.isEmpty();

        if (sectionRestricted && status != null) {
            return reportRepo.findByStatusAndSectionIdInOrderByCreatedAtDesc(status, s.sectionIds);
        }
        if (sectionRestricted) {
            return reportRepo.findBySectionIdInOrderByCreatedAtDesc(s.sectionIds);
        }
        if (groupRestricted && status != null) {
            return reportRepo.findByStatusAndGroupIdInOrderByCreatedAtDesc(status, s.groupIds);
        }
        if (groupRestricted) {
            return reportRepo.findByGroupIdInOrderByCreatedAtDesc(s.groupIds);
        }
        if (status != null) {
            return reportRepo.findByStatusOrderByCreatedAtDesc(status);
        }
        return reportRepo.findAllByOrderByCreatedAtDesc();
    }

    /**
     * 统计：按状态、按课，返回给当前用户可访问的范围
     */
    public Map<String, Object> statistics(LocalDateTime from, LocalDateTime to) {
        DataScope.Scope s = dataScope.currentScope();
        List<Long> sectionIds = s.sectionIds.isEmpty() ? null : s.sectionIds;
        List<Long> groupIds = s.groupIds.isEmpty() ? null : s.groupIds;

        // 按状态
        List<Object[]> byStatus = reportRepo.countByStatusInScope(sectionIds, groupIds, from, to);
        Map<String, Long> statusMap = new LinkedHashMap<>();
        long total = 0;
        for (ExceptionStatus os : ExceptionStatus.values()) statusMap.put(os.name(), 0L);
        for (Object[] row : byStatus) {
            ExceptionStatus st = (ExceptionStatus) row[0];
            Long cnt = ((Number) row[1]).longValue();
            statusMap.put(st.name(), cnt);
            total += cnt;
        }
        statusMap.put("TOTAL", total);

        // 按课
        List<Object[]> bySection = reportRepo.countBySectionInScope(sectionIds, groupIds, from, to);
        Map<String, Long> sectionMap = new LinkedHashMap<>();
        for (Object[] row : bySection) {
            Long secId = (Long) row[0];
            Long cnt = ((Number) row[1]).longValue();
            String name = deptRepo.findById(secId).map(Department::getName).orElse("#" + secId);
            sectionMap.put(name, cnt);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("byStatus", statusMap);
        result.put("bySection", sectionMap);
        result.put("total", total);
        return result;
    }

    public List<ExceptionReport> listByStatus(ExceptionStatus status) {
        if (status == null) {
            return reportRepo.findAllByOrderByCreatedAtDesc();
        }
        return reportRepo.findByStatusOrderByCreatedAtDesc(status);
    }

    public List<Department> listSections() {
        return deptRepo.findByType(DeptType.SECTION);
    }

    public List<Department> listGroups(Long sectionId) {
        return deptRepo.findByParentId(sectionId);
    }

    public List<Department> listLines(Long groupId) {
        return deptRepo.findByParentId(groupId);
    }

    public List<Process> listProcesses(Long lineId) {
        return processRepo.findByLineId(lineId);
    }

    public Map<String, Object> resolveNames(ExceptionReport r) {
        Map<String, Object> m = new HashMap<>();
        m.put("section", deptRepo.findById(r.getSectionId()).map(Department::getName).orElse("-"));
        m.put("group", deptRepo.findById(r.getGroupId()).map(Department::getName).orElse("-"));
        m.put("line", deptRepo.findById(r.getLineId()).map(Department::getName).orElse("-"));
        m.put("process", r.getProcessId() == null ? "-" :
                processRepo.findById(r.getProcessId()).map(Process::getName).orElse("-"));
        return m;
    }

    public List<User> listUsers() {
        return userRepo.findAll();
    }

    public Optional<User> findUser(Long id) {
        return userRepo.findById(id);
    }
}
