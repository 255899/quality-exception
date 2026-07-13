package com.gzklc.quality.controller;

import com.gzklc.quality.dto.ApiResponse;
import com.gzklc.quality.dto.ApplyRequest;
import com.gzklc.quality.dto.ExceptionDetailDTO;
import com.gzklc.quality.dto.FlowActionRequest;
import com.gzklc.quality.entity.ExceptionReport;
import com.gzklc.quality.enums.ExceptionStatus;
import com.gzklc.quality.enums.FlowStep;
import com.gzklc.quality.service.ExceptionService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exceptions")
@RequiredArgsConstructor
public class ExceptionController {

    private final ExceptionService service;

    /** 预览编号 */
    @GetMapping("/preview-code")
    public ApiResponse<String> previewCode() {
        return ApiResponse.ok(service.previewCode());
    }

    /** 提交申请 */
    @PostMapping("/apply")
    public ApiResponse<ExceptionReport> apply(@RequestBody @Valid ApplyRequest req) {
        return ApiResponse.ok(service.apply(req));
    }

    /** 详情（含流程） */
    @GetMapping("/{id}")
    public ApiResponse<ExceptionDetailDTO> detail(@PathVariable Long id) {
        return ApiResponse.ok(service.detail(id));
    }

    /** 列表（按当前登录用户的数据范围，可按状态过滤） */
    @GetMapping
    public ApiResponse<List<ExceptionReport>> list(@RequestParam(required = false) ExceptionStatus status) {
        return ApiResponse.ok(service.listInScope(status));
    }

    /** 统计（按当前登录用户的数据范围） */
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> statistics(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        java.time.LocalDateTime f = from == null || from.isEmpty() ? null : java.time.LocalDateTime.parse(from);
        java.time.LocalDateTime t = to == null || to.isEmpty() ? null : java.time.LocalDateTime.parse(to);
        return ApiResponse.ok(service.statistics(f, t));
    }

    /** 通用流程处理：step=QC_HANDLE / SHIFT_CONFIRM / QC_CONFIRM / LEADER_CONFIRM / CHIEF_CONFIRM / QUALITY_CONFIRM */
    @PostMapping("/flow/{step}")
    public ApiResponse<ExceptionReport> flow(@PathVariable FlowStep step,
                                             @RequestBody @Valid FlowActionRequest req) {
        return ApiResponse.ok(service.process(step, req));
    }
}
