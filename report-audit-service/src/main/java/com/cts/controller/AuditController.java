package com.cts.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cts.dto.AuditPackageDTO;
import com.cts.response.ApiResponse;
import com.cts.service.AuditPackageService;

@RestController
@RequestMapping("/api/audit-packages")
public class AuditController {

    @Autowired
    private AuditPackageService auditService;

    
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<AuditPackageDTO>> generate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "ALL") String scope,
            @RequestHeader(value = "X-Auth-User", defaultValue = "SYSTEM") String generatedBy) {

        AuditPackageDTO generated = auditService.generatePackage(start, end, scope, generatedBy);
        return new ResponseEntity<>(
            ApiResponse.success(
                "Audit package generated | scope: " + scope.toUpperCase()
                + " | period: " + start + " to " + end
                + " | by: " + generatedBy,
                generated),
            HttpStatus.CREATED
        );
    }

    // ─────────────────────────────────────────────
    // CRUD
    // ─────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<AuditPackageDTO>> createPackage(
            @RequestBody AuditPackageDTO dto) {
        AuditPackageDTO created = auditService.createPackage(dto);
        return new ResponseEntity<>(
            ApiResponse.success("Audit package created manually", created), HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditPackageDTO>>> listAll() {
        return ResponseEntity.ok(
            ApiResponse.success("All audit packages retrieved", auditService.getAllPackages())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditPackageDTO>> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success("Audit package found", auditService.getPackageById(id))
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<AuditPackageDTO>>> searchByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(
            ApiResponse.success("Audit packages found for period",
                auditService.getPackagesByPeriod(start, end))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditPackageDTO>> updatePackage(
            @PathVariable Long id, @RequestBody AuditPackageDTO dto) {
        return ResponseEntity.ok(
            ApiResponse.success("Audit package updated", auditService.updatePackage(id, dto))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deletePackage(@PathVariable Long id) {
        auditService.deletePackage(id);
        return ResponseEntity.ok(
            ApiResponse.success("Success", "Audit Package " + id + " deleted.")
        );
    }
}