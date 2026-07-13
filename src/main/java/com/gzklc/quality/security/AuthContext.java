package com.gzklc.quality.security;

import com.gzklc.quality.entity.User;
import lombok.Data;

/**
 * 登录后的当前用户上下文（按线程隔离）
 */
public class AuthContext {

    private static final ThreadLocal<User> CURRENT = new ThreadLocal<>();

    public static void set(User u) { CURRENT.set(u); }
    public static User get() { return CURRENT.get(); }
    public static void clear() { CURRENT.remove(); }

    public static Long id() { User u = get(); return u == null ? null : u.getId(); }
    public static String role() { User u = get(); return u == null ? null : u.getRole(); }
    public static Long deptId() { User u = get(); return u == null ? null : u.getDeptId(); }
}
