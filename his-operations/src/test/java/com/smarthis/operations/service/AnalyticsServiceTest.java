package com.smarthis.operations.service;

import com.smarthis.common.context.UserContext;
import com.smarthis.common.context.UserContextHolder;
import com.smarthis.operations.dto.response.AnalyticsSummary;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class AnalyticsServiceTest {
    private static final LocalDate FROM = LocalDate.of(2024, 2, 1);
    private static final LocalDate TO = LocalDate.of(2024, 2, 29);
    private static final long USER_DEPT_ID = 1001L;
    private static final long REQUESTED_DEPT_ID = 4_294_967_296L;

    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final AnalyticsService service = new AnalyticsService(jdbc);

    @AfterEach
    void clearContext() {
        UserContextHolder.clear();
    }

    static Stream<Arguments> departmentScopes() {
        return Stream.of(
                Arguments.of("ADMIN", null, null),
                Arguments.of("ADMIN", REQUESTED_DEPT_ID, REQUESTED_DEPT_ID),
                Arguments.of("INPATIENT_DOCTOR", null, USER_DEPT_ID),
                Arguments.of("INPATIENT_DOCTOR", REQUESTED_DEPT_ID, USER_DEPT_ID));
    }

    @ParameterizedTest(name = "{0}: requestedDeptId={1}, effectiveDeptId={2}")
    @MethodSource("departmentScopes")
    void summaryPreservesAllQueriesAndParameters(String role, Long requestedDeptId, Long effectiveDeptId) {
        setContext(role);
        when(jdbc.queryForObject(anyString(), eq(Long.class), any(Object[].class))).thenReturn(12L);
        when(jdbc.queryForMap(anyString(), any(Object[].class))).thenReturn(Map.of(
                "bill_count", 7L,
                "billed_amount", new BigDecimal("100.1234"),
                "received_amount", new BigDecimal("80.5678")));
        when(jdbc.queryForObject(anyString(), eq(BigDecimal.class), any(Object[].class)))
                .thenReturn(new BigDecimal("5.4321"));
        when(jdbc.queryForMap(anyString())).thenReturn(Map.of(
                "quantity", new BigDecimal("500.2500"),
                "expiring", new BigDecimal("20.1250")));

        AnalyticsSummary result = service.summary(FROM, TO, requestedDeptId);

        assertEquals(new AnalyticsSummary(12L, 7L,
                new BigDecimal("100.1234"), new BigDecimal("80.5678"), new BigDecimal("5.4321"),
                new BigDecimal("500.2500"), new BigDecimal("20.1250")), result);
        Object[] expectedArgs = {Date.valueOf(FROM), Date.valueOf("2024-03-01"), effectiveDeptId, effectiveDeptId};
        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> args = ArgumentCaptor.forClass(Object[].class);

        verify(jdbc).queryForObject(sql.capture(), eq(Long.class), args.capture());
        assertEquals("SELECT count(*) FROM his_patient.pat_registration "
                + "WHERE deleted=0 AND reg_date>=? AND reg_date<? AND (CAST(? AS BIGINT) IS NULL OR dept_id=?)",
                sql.getValue());
        assertArrayEquals(expectedArgs, args.getValue());

        verify(jdbc).queryForMap(sql.capture(), args.capture());
        assertEquals("SELECT count(*) bill_count, COALESCE(sum(payable_amount),0) billed_amount, "
                + "COALESCE(sum(paid_amount),0) received_amount FROM ops_bill WHERE deleted=0 AND created_time>=? AND created_time<? "
                + "AND (CAST(? AS BIGINT) IS NULL OR dept_id=?)", sql.getValue());
        assertArrayEquals(expectedArgs, args.getValue());

        verify(jdbc).queryForObject(sql.capture(), eq(BigDecimal.class), args.capture());
        assertEquals("SELECT COALESCE(sum(t.amount),0) FROM ops_bill_transaction t "
                + "JOIN ops_bill b ON b.id=t.bill_id WHERE t.deleted=0 AND b.deleted=0 AND t.transaction_type='REFUND' "
                + "AND t.transaction_time>=? AND t.transaction_time<? AND (CAST(? AS BIGINT) IS NULL OR b.dept_id=?)",
                sql.getValue());
        assertArrayEquals(expectedArgs, args.getValue());

        verify(jdbc).queryForMap(sql.capture());
        assertEquals("SELECT COALESCE(sum(quantity),0) quantity, "
                + "COALESCE(sum(CASE WHEN expiry_date<=CURRENT_DATE+90 THEN available_quantity ELSE 0 END),0) expiring "
                + "FROM his_pharma.pha_inventory_batch WHERE deleted=0 AND is_active=1", sql.getValue());
        verifyNoMoreInteractions(jdbc);
    }

    @ParameterizedTest(name = "{0}: requestedDeptId={1}, effectiveDeptId={2}")
    @MethodSource("departmentScopes")
    void departmentPerformancePreservesQueryAndParameters(String role, Long requestedDeptId, Long effectiveDeptId) {
        setContext(role);
        List<Map<String, Object>> rows = List.of(Map.of(
                "dept_id", USER_DEPT_ID, "bill_count", 7L,
                "billed_amount", new BigDecimal("100.1234"), "received_amount", new BigDecimal("80.5678")));
        when(jdbc.queryForList(anyString(), any(Object[].class))).thenReturn(rows);

        assertSame(rows, service.departmentPerformance(FROM, TO, requestedDeptId));

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> args = ArgumentCaptor.forClass(Object[].class);
        verify(jdbc).queryForList(sql.capture(), args.capture());
        assertEquals("SELECT dept_id, count(*) bill_count, COALESCE(sum(payable_amount),0) billed_amount, "
                + "COALESCE(sum(paid_amount),0) received_amount FROM ops_bill WHERE deleted=0 AND created_time>=? AND created_time<? "
                + "AND (CAST(? AS BIGINT) IS NULL OR dept_id=?) GROUP BY dept_id ORDER BY received_amount DESC",
                sql.getValue());
        assertArrayEquals(new Object[]{Date.valueOf(FROM), Date.valueOf("2024-03-01"), effectiveDeptId, effectiveDeptId},
                args.getValue());
        verifyNoMoreInteractions(jdbc);
    }

    @ParameterizedTest(name = "{0}: requestedDeptId={1}, effectiveDeptId={2}")
    @MethodSource("departmentScopes")
    void doctorWorkloadPreservesQueryAndInclusiveEndDate(String role, Long requestedDeptId, Long effectiveDeptId) {
        setContext(role);
        List<Map<String, Object>> rows = List.of(Map.of("doctor_id", 20L, "dept_id", USER_DEPT_ID, "visit_count", 12L));
        when(jdbc.queryForList(anyString(), any(Object[].class))).thenReturn(rows);

        assertSame(rows, service.doctorWorkload(FROM, TO, requestedDeptId));

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> args = ArgumentCaptor.forClass(Object[].class);
        verify(jdbc).queryForList(sql.capture(), args.capture());
        assertEquals("SELECT doctor_id, dept_id, count(*) visit_count FROM his_patient.pat_registration "
                + "WHERE deleted=0 AND reg_status<>'CANCELLED' AND reg_date BETWEEN ? AND ? AND (CAST(? AS BIGINT) IS NULL OR dept_id=?) "
                + "GROUP BY doctor_id, dept_id ORDER BY visit_count DESC", sql.getValue());
        assertArrayEquals(new Object[]{Date.valueOf(FROM), Date.valueOf(TO), effectiveDeptId, effectiveDeptId}, args.getValue());
        verifyNoMoreInteractions(jdbc);
    }

    private void setContext(String role) {
        UserContext context = new UserContext();
        context.setRoles(role);
        context.setDeptId(USER_DEPT_ID);
        UserContextHolder.set(context);
    }
}
