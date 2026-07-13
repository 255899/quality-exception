package com.gzklc.quality.controller;

import com.gzklc.quality.dto.ApiResponse;
import com.gzklc.quality.entity.ExceptionAttachment;
import com.gzklc.quality.entity.User;
import com.gzklc.quality.repository.ExceptionAttachmentRepository;
import com.gzklc.quality.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 公共数据：品管人员、异常发生选项、上传
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommonController {

    private final UserRepository userRepo;
    private final ExceptionAttachmentRepository attachmentRepo;

    /** 品管人员列表 */
    @GetMapping("/users/qc")
    public ApiResponse<List<User>> qcUsers() {
        return ApiResponse.ok(userRepo.findByRole("QC"));
    }

    /** 异常发生选项（9 项） */
    @GetMapping("/exception-reasons")
    public ApiResponse<List<Map<String, String>>> reasons() {
        List<Map<String, String>> list = new ArrayList<>();
        list.add(reason("1", "刀具崩落异常磨损时"));
        list.add(reason("2", "加工途中设备突然停止时"));
        list.add(reason("3", "夹、治具破损及设备故障时"));
        list.add(reason("4", "定期检查发现产品尺寸超出规格时"));
        list.add(reason("5", "突然发生停电、灾害事件致使设备停止时"));
        list.add(reason("6", "自动检测机发生故障、量具损坏、缺失不能达到使用目的时"));
        list.add(reason("7", "设备运转中发生异音、异味及异常振动时"));
        list.add(reason("8", "条形码代号与部品实际不相符时"));
        list.add(reason("9", "非正常的产品部品为异常品"));
        return ApiResponse.ok(list);
    }

    private Map<String, String> reason(String code, String text) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("code", code);
        m.put("text", text);
        return m;
    }

    /** 文件上传（图片），返回附件 ID + URL */
    @PostMapping("/upload")
    public ApiResponse<Map<String, Object>> upload(@RequestParam("file") MultipartFile file, HttpServletRequest req) throws IOException {
        if (file.isEmpty()) {
            return ApiResponse.error("文件为空");
        }
        String root = new File(System.getProperty("user.dir"), "uploads").getAbsolutePath();
        LocalDate d = LocalDate.now();
        String sub = String.format("%04d-%02d-%02d", d.getYear(), d.getMonthValue(), d.getDayOfMonth());
        File dir = new File(root, sub);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("无法创建目录: " + dir);
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.lastIndexOf('.') > 0) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String name = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = Paths.get(dir.getAbsolutePath(), name);
        Files.write(target, file.getBytes());

        ExceptionAttachment att = new ExceptionAttachment();
        att.setFileName(original);
        att.setFilePath(sub + "/" + name);
        String url = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort()
                + "/uploads/" + sub + "/" + name;
        att.setUrl(url);
        att.setSize(file.getSize());
        att.setContentType(file.getContentType());
        att.setUploadedAt(LocalDateTime.now());
        attachmentRepo.save(att);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", att.getId());
        data.put("url", url);
        data.put("fileName", original);
        data.put("size", file.getSize());
        return ApiResponse.ok(data);
    }
}
