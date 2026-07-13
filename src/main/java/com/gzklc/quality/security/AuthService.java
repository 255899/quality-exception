package com.gzklc.quality.security;

import com.gzklc.quality.entity.User;
import com.gzklc.quality.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单的 token 鉴权（演示用）。生产请用 Spring Security + JWT。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    /** token -> userId */
    private final Map<String, Long> tokens = new ConcurrentHashMap<>();

    public Map<String, Object> login(String username, String password) {
        if (username == null || password == null) {
            throw new IllegalArgumentException("请输入用户名和密码");
        }
        User u = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if (u.getEnabled() != null && !u.getEnabled()) {
            throw new IllegalArgumentException("账号已停用");
        }
        if (!password.equals(u.getPassword())) {
            throw new IllegalArgumentException("密码错误");
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        tokens.put(token, u.getId());
        java.util.LinkedHashMap<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("token", token);
        data.put("userId", u.getId());
        data.put("username", u.getUsername());
        data.put("realName", u.getRealName());
        data.put("role", u.getRole());
        data.put("deptId", u.getDeptId());
        return data;
    }

    public void logout(String token) {
        if (token != null) tokens.remove(token);
    }

    public Optional<User> findByToken(String token) {
        if (token == null) return Optional.empty();
        Long uid = tokens.get(token);
        if (uid == null) return Optional.empty();
        return userRepo.findById(uid);
    }
}
