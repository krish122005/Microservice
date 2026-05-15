package com.cts.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cts.dto.ReportDTO;
import com.cts.response.ApiResponse;
import com.cts.service.ReportService;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<ReportDTO>> generate(
            @RequestParam String scope,
            @RequestParam(defaultValue = "ALL") String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer id,
            @RequestHeader(value = "X-Auth-User", defaultValue = "SYSTEM") String generatedBy) {

        ReportDTO result = reportService.generateReport(scope, type, status, id, generatedBy);
        return ResponseEntity.ok(
            ApiResponse.success(
                "Report generated | scope: " + scope.toUpperCase()
                + " | type: " + type.toUpperCase()
                + " | by: " + generatedBy,
                result)
        );
    }

    // ─────────────────────────────────────────────
    // CRUD
    // ─────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<ReportDTO>> createReport(
            @RequestBody ReportDTO reportDTO) {
        ReportDTO created = reportService.createReport(reportDTO);
        return new ResponseEntity<>(
            ApiResponse.success("Report record saved", created), HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReportDTO>>> listReports() {
        return ResponseEntity.ok(
            ApiResponse.success("All reports retrieved", reportService.getAllReportsDTO())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReportDTO>> getReportById(@PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success("Report found", reportService.getReportDTOById(id))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteById(@PathVariable Long id) {
        reportService.deleteReportById(id);
        return ResponseEntity.ok(ApiResponse.success("Success", "Report " + id + " deleted."));
    }
}