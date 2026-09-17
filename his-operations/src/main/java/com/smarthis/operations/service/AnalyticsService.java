package com.smarthis.operations.service;

import com.smarthis.common.security.DataScope;
import com.smarthis.operations.dto.response.AnalyticsSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final JdbcTemplate jdbc;

    public AnalyticsSummary summary(LocalDate from, LocalDate to, Long requestedDeptId) {
        LocalDate start = from != null ? from : LocalDate.now().minusDays(29);
        LocalDate endExclusive = (to != null ? to : LocalDate.now()).plusDays(1);
        Long deptId = DataScope.restrictDepartment(requestedDeptId);
        Object[] args = {Date.valueOf(start), Date.valueOf(endExclusive), deptId, deptId};
        long outpatient = number(jdbc.queryForObject("SELECT count(*) FROM his_patient.pat_registration "
                + "WHERE deleted=0 AND reg_date>=? AND reg_date<? AND (? IS NULL OR dept_id=?)", Long.class, args)).longValue();
        Map<String, Object> bills = jdbc.queryForMap("SELECT count(*) bill_count, COALESCE(sum(payable_amount),0) billed_amount, "
                + "COALESCE(sum(paid_amount),0) received_amount FROM ops_bill WHERE deleted=0 AND created_time>=? AND created_time<? "
                + "AND (? IS NULL OR dept_id=?)", args);
        BigDecimal refunds = number(jdbc.queryForObject("SELECT COALESCE(sum(t.amount),0) FROM ops_bill_transaction t "
                + "JOIN ops_bill b ON b.id=t.bill_id WHERE t.deleted=0 AND b.deleted=0 AND t.transaction_type='REFUND' "
                + "AND t.transaction_time>=? AND t.transaction_time<? AND (? IS NULL OR b.dept_id=?)", BigDecimal.class, args));
        Map<String, Object> inventory = jdbc.queryForMap("SELECT COALESCE(sum(quantity),0) quantity, "
                + "COALESCE(sum(CASE WHEN expiry_date<=CURRENT_DATE+90 THEN available_quantity ELSE 0 END),0) expiring "
                + "FROM his_pharma.pha_inventory_batch WHERE deleted=0 AND is_active=1");
        return new AnalyticsSummary(outpatient, number(bills.get("bill_count")).longValue(),
                number(bills.get("billed_amount")), number(bills.get("received_amount")), refunds,
                number(inventory.get("quantity")), number(inventory.get("expiring")));
    }

    public List<Map<String, Object>> departmentPerformance(LocalDate from, LocalDate to, Long requestedDeptId) {
        LocalDate start = from != null ? from : LocalDate.now().minusDays(29);
        LocalDate endExclusive = (to != null ? to : LocalDate.now()).plusDays(1);
        Long deptId = DataScope.restrictDepartment(requestedDeptId);
        return jdbc.queryForList("SELECT dept_id, count(*) bill_count, COALESCE(sum(payable_amount),0) billed_amount, "
                + "COALESCE(sum(paid_amount),0) received_amount FROM ops_bill WHERE deleted=0 AND created_time>=? AND created_time<? "
                + "AND (? IS NULL OR dept_id=?) GROUP BY dept_id ORDER BY received_amount DESC",
                Date.valueOf(start), Date.valueOf(endExclusive), deptId, deptId);
    }

    public List<Map<String, Object>> doctorWorkload(LocalDate from, LocalDate to, Long requestedDeptId) {
        LocalDate start = from != null ? from : LocalDate.now().minusDays(29);
        LocalDate end = to != null ? to : LocalDate.now();
        Long deptId = DataScope.restrictDepartment(requestedDeptId);
        return jdbc.queryForList("SELECT doctor_id, dept_id, count(*) visit_count FROM his_patient.pat_registration "
                + "WHERE deleted=0 AND reg_status<>'CANCELLED' AND reg_date BETWEEN ? AND ? AND (? IS NULL OR dept_id=?) "
                + "GROUP BY doctor_id, dept_id ORDER BY visit_count DESC", Date.valueOf(start), Date.valueOf(end), deptId, deptId);
    }

    public Map<String, Object> exportRows(LocalDate from, LocalDate to, Long deptId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summary(from, to, deptId));
        result.put("departments", departmentPerformance(from, to, deptId));
        result.put("doctors", doctorWorkload(from, to, deptId));
        return result;
    }

    private static BigDecimal number(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal decimal) return decimal;
        return new BigDecimal(value.toString());
    }
}
