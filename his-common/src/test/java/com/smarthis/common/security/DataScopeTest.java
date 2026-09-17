package com.smarthis.common.security;

import com.smarthis.common.context.UserContext;
import com.smarthis.common.context.UserContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataScopeTest {
    @AfterEach void clear() { UserContextHolder.clear(); }

    @Test void departmentUserCannotQueryAnotherDepartment() {
        UserContext context = new UserContext();
        context.setRoles("INPATIENT_DOCTOR");
        context.setDeptId(1001L);
        UserContextHolder.set(context);
        assertEquals(1001L, DataScope.restrictDepartment(9999L));
    }

    @Test void administratorCanChooseDepartment() {
        UserContext context = new UserContext();
        context.setRoles("ADMIN");
        context.setDeptId(1001L);
        UserContextHolder.set(context);
        assertEquals(9999L, DataScope.restrictDepartment(9999L));
    }
}
