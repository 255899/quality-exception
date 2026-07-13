package com.gzklc.quality.security;

import com.gzklc.quality.entity.Department;
import com.gzklc.quality.entity.User;
import com.gzklc.quality.enums.DeptType;
import com.gzklc.quality.repository.DepartmentRepository;
import com.gzklc.quality.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 数据访问范围控制：按角色返回可访问的 sectionId 集合 / groupId 集合。
 *   - QUALITY_CHIEF：全部
 *   - SECTION_CHIEF / SECTION_LEADER：本课
 *   - SHIFT_LEADER：本组
 *   - QC：全部（品管跨课）
 */
@Component
@RequiredArgsConstructor
public class DataScope {

    private final DepartmentRepository deptRepo;
    private final UserRepository userRepo;

    public Scope currentScope() {
        User u = AuthContext.get();
        if (u == null) return new Scope(Collections.emptyList(), Collections.emptyList());
        String role = u.getRole();
        if ("QUALITY_CHIEF".equals(role) || "QC".equals(role)) {
            return Scope.all();
        }
        if ("SHIFT_LEADER".equals(role)) {
            if (u.getDeptId() == null) return new Scope(Collections.emptyList(), Collections.emptyList());
            return new Scope(Collections.emptyList(), Collections.singletonList(u.getDeptId()));
        }
        // SECTION_CHIEF / SECTION_LEADER：本课
        if ("SECTION_CHIEF".equals(role) || "SECTION_LEADER".equals(role)) {
            if (u.getDeptId() == null) return new Scope(Collections.emptyList(), Collections.emptyList());
            // deptId 是 section（课）
            Long sectionId = u.getDeptId();
            List<Long> sectionIds = Collections.singletonList(sectionId);
            // 收集组 ID 用于 group 过滤
            List<Department> groups = deptRepo.findByParentId(sectionId);
            List<Long> groupIds = new ArrayList<>();
            for (Department g : groups) groupIds.add(g.getId());
            return new Scope(sectionIds, groupIds);
        }
        return new Scope(Collections.emptyList(), Collections.emptyList());
    }

    public boolean canViewReport(User u, Long sectionId, Long groupId) {
        if (u == null) return false;
        String role = u.getRole();
        if ("QUALITY_CHIEF".equals(role) || "QC".equals(role)) return true;
        if ("SHIFT_LEADER".equals(role)) {
            return u.getDeptId() != null && u.getDeptId().equals(groupId);
        }
        if ("SECTION_CHIEF".equals(role) || "SECTION_LEADER".equals(role)) {
            return u.getDeptId() != null && u.getDeptId().equals(sectionId);
        }
        return false;
    }

    public static class Scope {
        public final List<Long> sectionIds;  // empty = 不限制
        public final List<Long> groupIds;    // empty = 不限制
        public Scope(List<Long> s, List<Long> g) { this.sectionIds = s; this.groupIds = g; }
        public boolean isUnrestricted() { return sectionIds.isEmpty() && groupIds.isEmpty(); }
        public static Scope all() { return new Scope(Collections.emptyList(), Collections.emptyList()); }
    }
}
