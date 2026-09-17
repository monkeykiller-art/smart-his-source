package com.smarthis.operations.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthis.common.context.UserContextHolder;
import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.security.RequiresPermission;
import com.smarthis.operations.dto.response.AnalyticsSummary;
import com.smarthis.operations.entity.SavedReportFilter;
import com.smarthis.operations.mapper.SavedReportFilterMapper;
import com.smarthis.operations.service.AnalyticsService;
import com.smarthis.operations.support.SimplePdf;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/operations/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsService analyticsService;
    private final SavedReportFilterMapper savedFilterMapper;

    @GetMapping("/summary")
    @RequiresPermission("operations:revenue:stat")
    public ApiResponse<AnalyticsSummary> summary(@RequestParam(required = false) LocalDate from,
                                                 @RequestParam(required = false) LocalDate to,
                                                 @RequestParam(required = false) Long deptId) {
        return ApiResponse.ok(analyticsService.summary(from, to, deptId));
    }

    @GetMapping("/departments")
    @RequiresPermission("operations:revenue:stat")
    public ApiResponse<List<Map<String, Object>>> departments(@RequestParam(required = false) LocalDate from,
                                                               @RequestParam(required = false) LocalDate to,
                                                               @RequestParam(required = false) Long deptId) {
        return ApiResponse.ok(analyticsService.departmentPerformance(from, to, deptId));
    }

    @GetMapping("/doctors")
    @RequiresPermission("operations:workload:stat")
    public ApiResponse<List<Map<String, Object>>> doctors(@RequestParam(required = false) LocalDate from,
                                                           @RequestParam(required = false) LocalDate to,
                                                           @RequestParam(required = false) Long deptId) {
        return ApiResponse.ok(analyticsService.doctorWorkload(from, to, deptId));
    }

    @GetMapping("/export.xls")
    @RequiresPermission("operations:revenue:stat")
    public ResponseEntity<byte[]> exportExcel(@RequestParam(required = false) LocalDate from,
                                               @RequestParam(required = false) LocalDate to,
                                               @RequestParam(required = false) Long deptId) {
        AnalyticsSummary report = analyticsService.summary(from, to, deptId);
        String html = "<html><meta charset=\"UTF-8\"><table border=\"1\"><tr><th>Metric</th><th>Value</th></tr>"
                + row("Outpatient visits", report.outpatientVisits()) + row("Bill count", report.billCount())
                + row("Billed amount", report.billedAmount()) + row("Received amount", report.receivedAmount())
                + row("Refunded amount", report.refundedAmount()) + row("Inventory quantity", report.inventoryQuantity())
                + row("Expiring inventory", report.expiringInventoryQuantity()) + "</table></html>";
        return download(html.getBytes(StandardCharsets.UTF_8), "smart-his-report.xls", "application/vnd.ms-excel");
    }

    @GetMapping("/export.pdf")
    @RequiresPermission("operations:revenue:stat")
    public ResponseEntity<byte[]> exportPdf(@RequestParam(required = false) LocalDate from,
                                             @RequestParam(required = false) LocalDate to,
                                             @RequestParam(required = false) Long deptId) {
        AnalyticsSummary report = analyticsService.summary(from, to, deptId);
        List<String> lines = new ArrayList<>();
        lines.add("Smart HIS Operations Report");
        lines.add("Period: " + (from == null ? "last 30 days" : from) + " to " + (to == null ? LocalDate.now() : to));
        lines.add("Outpatient visits: " + report.outpatientVisits());
        lines.add("Bills: " + report.billCount());
        lines.add("Billed: " + report.billedAmount());
        lines.add("Received: " + report.receivedAmount());
        lines.add("Refunded: " + report.refundedAmount());
        lines.add("Inventory: " + report.inventoryQuantity());
        return download(SimplePdf.create(lines), "smart-his-report.pdf", MediaType.APPLICATION_PDF_VALUE);
    }

    @GetMapping("/filters")
    @RequiresPermission("operations:revenue:stat")
    public ApiResponse<List<SavedReportFilter>> filters(@RequestParam(required = false) String reportCode) {
        return ApiResponse.ok(savedFilterMapper.selectList(new LambdaQueryWrapper<SavedReportFilter>()
                .eq(SavedReportFilter::getUserId, UserContextHolder.getUserId())
                .eq(reportCode != null && !reportCode.isBlank(), SavedReportFilter::getReportCode, reportCode)
                .eq(SavedReportFilter::getDeleted, 0).orderByDesc(SavedReportFilter::getCreatedTime)));
    }

    @PostMapping("/filters")
    @RequiresPermission("operations:revenue:stat")
    public ApiResponse<SavedReportFilter> saveFilter(@Valid @RequestBody SavedReportFilter filter) {
        filter.setId(null);
        filter.setUserId(UserContextHolder.getUserId());
        savedFilterMapper.insert(filter);
        return ApiResponse.ok(filter);
    }

    @DeleteMapping("/filters/{id}")
    @RequiresPermission("operations:revenue:stat")
    public ApiResponse<Void> deleteFilter(@PathVariable Long id) {
        savedFilterMapper.delete(new LambdaQueryWrapper<SavedReportFilter>()
                .eq(SavedReportFilter::getId, id).eq(SavedReportFilter::getUserId, UserContextHolder.getUserId()));
        return ApiResponse.ok();
    }

    private static String row(String label, Object value) { return "<tr><td>" + label + "</td><td>" + value + "</td></tr>"; }
    private static ResponseEntity<byte[]> download(byte[] body, String filename, String mediaType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mediaType));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(body);
    }
}
