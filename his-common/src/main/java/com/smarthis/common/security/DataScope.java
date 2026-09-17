package com.smarthis.common.security;

import com.smarthis.common.context.UserContext;
import com.smarthis.common.context.UserContextHolder;

import java.util.Arrays;

public final class DataScope {
    private DataScope() {}

    public static Long restrictDepartment(Long requestedDepartmentId) {
        UserContext context = UserContextHolder.get();
        if (context == null || context.getRoles() == null) return requestedDepartmentId;
        boolean global = Arrays.stream(context.getRoles().split(","))
                .map(String::trim)
                .anyMatch("ADMIN"::equals);
        return global ? requestedDepartmentId : context.getDeptId();
    }
}
