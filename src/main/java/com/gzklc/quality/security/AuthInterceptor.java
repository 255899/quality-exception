package com.gzklc.quality.security;

import com.gzklc.quality.entity.User;
import com.gzklc.quality.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

/**
 * 鉴权拦截器：
 *   - 在受保护接口上校验 X-Auth-Token / token 查询参数 / Bearer
 *   - 不通过则返回 401
 *   - 通过则把当前用户放进 AuthContext
 *
 * 多层访问控制策略：
 *   1) 任何管理/统计/流程相关接口必须登录
 *   2) apply / preview-code / upload / departments / users/qc / reasons 不强制登录
 *      （班长移动端申请时即便没登录也允许提交，applicantId 留空）
 *   3) 角色越权访问由 service 内部按数据范围控制
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;
    private final UserRepository userRepo;

    /** 不需要鉴权的路径前缀（开放给匿名端） */
    public static final java.util.Set<String> OPEN_PREFIX = new java.util.HashSet<>(java.util.Arrays.asList(
            "/api/auth/login",
            "/api/exceptions/apply",
            "/api/exceptions/preview-code",
            "/api/upload",
            "/api/departments",
            "/api/exception-reasons",
            "/api/users/qc"
    ));

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String path = request.getRequestURI();

        // 放行 OPTIONS（CORS 预检）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 放行公开接口
        for (String p : OPEN_PREFIX) {
            if (path.startsWith(p)) return true;
        }

        String token = extractToken(request);
        Optional<User> u = authService.findByToken(token);
        if (!u.isPresent()) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"请先登录\",\"data\":null}");
            return false;
        }
        AuthContext.set(u.get());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    private String extractToken(HttpServletRequest req) {
        String h = req.getHeader("X-Auth-Token");
        if (h != null && !h.isEmpty()) return h;
        String auth = req.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) return auth.substring(7);
        return req.getParameter("token");
    }
}
