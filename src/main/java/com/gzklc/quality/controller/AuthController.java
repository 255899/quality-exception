package com.gzklc.quality.controller;

import com.gzklc.quality.dto.ApiResponse;
import com.gzklc.quality.entity.User;
import com.gzklc.quality.repository.UserRepository;
import com.gzklc.quality.security.AuthContext;
import com.gzklc.quality.security.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepo;

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        return ApiResponse.ok(authService.login(body.get("username"), body.get("password")));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest req) {
        String token = req.getHeader("X-Auth-Token");
        if (token == null) {
            String auth = req.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) token = auth.substring(7);
        }
        authService.logout(token);
        return ApiResponse.ok();
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me() {
        User u = AuthContext.get();
        if (u == null) return ApiResponse.error(401, "未登录");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", u.getId());
        data.put("username", u.getUsername());
        data.put("realName", u.getRealName());
        data.put("role", u.getRole());
        data.put("deptId", u.getDeptId());
        return ApiResponse.ok(data);
    }
}
