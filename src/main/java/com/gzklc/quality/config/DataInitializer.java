package com.gzklc.quality.config;

import com.gzklc.quality.entity.Department;
import com.gzklc.quality.entity.Part;
import com.gzklc.quality.entity.Process;
import com.gzklc.quality.entity.User;
import com.gzklc.quality.enums.DeptType;
import com.gzklc.quality.repository.DepartmentRepository;
import com.gzklc.quality.repository.PartRepository;
import com.gzklc.quality.repository.ProcessRepository;
import com.gzklc.quality.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 启动时初始化基础数据
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DepartmentRepository deptRepo;
    private final ProcessRepository processRepo;
    private final UserRepository userRepo;
    private final PartRepository partRepo;

    @Override
    public void run(String... args) {
        if (deptRepo.count() > 0) {
            return;
        }
        // 课
        Department s1 = save("加工一课", DeptType.SECTION, null);
        Department s2 = save("加工二课", DeptType.SECTION, null);
        Department s3 = save("加工三课", DeptType.SECTION, null);

        // 组（每课下两个组：A组 / B组）
        Department s1g1 = save("A组", DeptType.GROUP, s1.getId());
        Department s1g2 = save("B组", DeptType.GROUP, s1.getId());
        Department s2g1 = save("A组", DeptType.GROUP, s2.getId());
        Department s2g2 = save("B组", DeptType.GROUP, s2.getId());
        Department s3g1 = save("A组", DeptType.GROUP, s3.getId());
        Department s3g2 = save("B组", DeptType.GROUP, s3.getId());

        // 产线 + 工程（每组两条产线、每条产线挂 2-3 个工程）
        // 组和产线的对应关系
        Object[][] groupLineSpec = {
            {s1g1, new String[]{"刹车盘1线", "刹车盘4线"}},
            {s1g2, new String[]{"刹车盘8线", "刹车盘6线"}},
            {s2g1, new String[]{"刹车鼓1线", "立邦涂装线"}},
            {s2g2, new String[]{"久美特涂装线", "刹车盘1线"}},
            {s3g1, new String[]{"刹车盘4线", "刹车盘8线"}},
            {s3g2, new String[]{"刹车鼓1线", "立邦涂装线"}}
        };
        // 每个工程挂哪些部品（code, name）
        String[][] standardParts = {
            {"43206 5RF0D", "日产5R鼓"},
            {"3510120EH1", "E38B前制动盘"},
            {"3510120EH3", "E29前制动盘"},
            {"3510160EA1", "E38A前制动盘"},
            {"3510120EH2", "E29前制动盘-打孔"},
            {"3520120EH2", "E29后制动盘-打孔"},
            {"3510120HD1", "H01前制动盘"},
            {"3510120GA1", "G01前制动盘"}
        };
        // 工程名（轮询挂到产线下）
        String[] engineeringNames = {
            "制动盘流程", "制动盘CCD检测",
            "制动盘OP10 外径端面粗加工", "制动盘OP20 内径端面粗加工",
            "制动盘OP30 外径端面精加工", "制动盘OP40 内径端面精加工",
            "制动盘OP50 钻孔", "制动盘OP60 滚光",
            "制动盘OP70 清洗"
        };
        int egIdx = 0;
        int partIdx = 0;
        for (Object[] row : groupLineSpec) {
            Department g = (Department) row[0];
            String[] lineNames = (String[]) row[1];
            for (String ln : lineNames) {
                Department line = save(ln, DeptType.LINE, g.getId());
                // 每条产线 2 个工程
                Process e1 = saveProc(engineeringNames[egIdx % engineeringNames.length], line.getId()); egIdx++;
                Process e2 = saveProc(engineeringNames[egIdx % engineeringNames.length], line.getId()); egIdx++;
                seedPart(e1, standardParts[partIdx % standardParts.length][0], standardParts[partIdx % standardParts.length][1]); partIdx++;
                seedPart(e2, standardParts[partIdx % standardParts.length][0], standardParts[partIdx % standardParts.length][1]); partIdx++;
            }
        }

        // 用户
        saveUser("shift_leader", "严松柏", "SHIFT_LEADER", s1g1.getId());
        saveUser("qc_zhang", "张品管", "QC", s1g1.getId());
        saveUser("qc_li", "李品管", "QC", s2g1.getId());
        saveUser("leader_wang", "王组长", "SECTION_LEADER", s1.getId());
        saveUser("leader_chen", "陈组长", "SECTION_LEADER", s2.getId());
        saveUser("chief_zhao", "赵课长", "SECTION_CHIEF", s1.getId());
        saveUser("chief_sun", "孙课长", "SECTION_CHIEF", s2.getId());
        saveUser("quality_chief", "周品质", "QUALITY_CHIEF", null);
        // 现场班长（仅申请 + 现场班长首件确认）
        saveUser("site_leader", "现场班长", "SHIFT_LEADER_SUPER", null);
    }

    private void seedPart(Process proc, String code, String name) {
        Part p = new Part();
        p.setProcessId(proc.getId());
        p.setCode(code);
        p.setName(name);
        partRepo.save(p);
    }

    private Department save(String name, DeptType type, Long parentId) {
        Department d = new Department();
        d.setName(name);
        d.setType(type);
        d.setParentId(parentId);
        return deptRepo.save(d);
    }

    private Process saveProc(String name, Long lineId) {
        Process p = new Process();
        p.setName(name);
        p.setLineId(lineId);
        return processRepo.save(p);
    }

    private void saveUser(String username, String realName, String role, Long deptId) {
        User u = new User();
        u.setUsername(username);
        u.setRealName(realName);
        u.setRole(role);
        u.setDeptId(deptId);
        // 演示账号：所有用户密码统一为 123456
        u.setPassword("123456");
        u.setEnabled(true);
        userRepo.save(u);
    }
}
