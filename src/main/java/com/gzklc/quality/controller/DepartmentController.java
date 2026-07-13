package com.gzklc.quality.controller;

import com.gzklc.quality.dto.ApiResponse;
import com.gzklc.quality.entity.Department;
import com.gzklc.quality.entity.Part;
import com.gzklc.quality.entity.Process;
import com.gzklc.quality.repository.PartRepository;
import com.gzklc.quality.service.ExceptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final ExceptionService service;
    private final PartRepository partRepo;

    /** 课列表 */
    @GetMapping("/sections")
    public ApiResponse<List<Department>> sections() {
        return ApiResponse.ok(service.listSections());
    }

    /** 组列表（按课） */
    @GetMapping("/groups")
    public ApiResponse<List<Department>> groups(@RequestParam Long sectionId) {
        return ApiResponse.ok(service.listGroups(sectionId));
    }

    /** 产线列表（按组） */
    @GetMapping("/lines")
    public ApiResponse<List<Department>> lines(@RequestParam Long groupId) {
        return ApiResponse.ok(service.listLines(groupId));
    }

    /** 工序列表（按产线） */
    @GetMapping("/processes")
    public ApiResponse<List<Process>> processes(@RequestParam Long lineId) {
        return ApiResponse.ok(service.listProcesses(lineId));
    }

    /** 部品列表（按工序） */
    @GetMapping("/parts")
    public ApiResponse<List<Part>> parts(@RequestParam Long processId) {
        return ApiResponse.ok(partRepo.findByProcessIdOrderByCodeAsc(processId));
    }
}
