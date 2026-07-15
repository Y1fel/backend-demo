package com.y1fel.backend.common.util;

public class UserContext {
    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        return get() == null ? null : get().id();
    }

    public static String getRealName() {
        return get() == null ? null : get().realName;
    }

    public static String getRole() {
        return get() == null ? null : get().role();
    }

    public static void clear() {
        HOLDER.remove();
    }

    public record LoginUser(Long id, String username, String realName, String role, Long orgId) {
    }
}
