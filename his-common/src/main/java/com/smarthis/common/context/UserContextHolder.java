package com.smarthis.common.context;

public final class UserContextHolder {

    private static final ThreadLocal<UserContext> HOLDER = new ThreadLocal<>();

    private UserContextHolder() {}

    public static void set(UserContext ctx) {
        HOLDER.set(ctx);
    }

    public static UserContext get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }

    public static Long getUserId() {
        UserContext ctx = HOLDER.get();
        return ctx != null ? ctx.getUserId() : null;
    }

    public static String getUsername() {
        UserContext ctx = HOLDER.get();
        return ctx != null ? ctx.getUsername() : "system";
    }

    public static String getRealName() {
        UserContext ctx = HOLDER.get();
        return ctx != null ? ctx.getRealName() : null;
    }

    public static Long getDeptId() {
        UserContext ctx = HOLDER.get();
        return ctx != null ? ctx.getDeptId() : null;
    }

    public static String getTraceId() {
        UserContext ctx = HOLDER.get();
        return ctx != null ? ctx.getTraceId() : null;
    }
}
