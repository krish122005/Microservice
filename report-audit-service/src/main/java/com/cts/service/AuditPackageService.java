package com.cts.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.dto.AuditPackageDTO;
import com.cts.dto.ReportDTO;
import com.cts.entity.AuditPackage;
import com.cts.exception.InvalidRequestException;
import com.cts.mapper.AuditPackageMapper;
import com.cts.repository.AuditRepository;

@Service
public class AuditPackageService {

    private final AuditRepository auditRepository;
    private final AuditPackageMapper auditPackageMapper;
    private final ReportService reportService;

    @Autowired
    public AuditPackageService(AuditRepository auditRepository,
                                AuditPackageMapper auditPackageMapper,
                                @Lazy ReportService reportService) {
        this.auditRepository = auditRepository;
        this.auditPackageMapper = auditPackageMapper;
        this.reportService = reportService;
    }

    // ─────────────────────────────────────────────
    // GENERATE — scope + generatedBy driven
    // POST /api/audit-packages/generate
    //      ?start=2026-01-01T00:00:00
    //      &end=2026-03-31T23:59:59
    //      &scope=ALL              (default ALL)
    //      &generatedBy=auditor.smith  (default SYSTEM)
    // ─────────────────────────────────────────────
    public AuditPackageDTO generatePackage(LocalDateTime start, LocalDateTime end,
                                            String scope, String generatedBy) {
        String contentsJSON;

        switch (scope.toUpperCase()) {

            case "REQUESTS" -> {
                String requestsMetrics = fetchScope("REQUESTS", generatedBy);
                contentsJSON = String.format(
                    "{ \"period\": \"%s to %s\", \"scope\": \"REQUESTS\", " +
                    "\"generatedBy\": \"%s\", \"requests\": %s }",
                    start, end, generatedBy, requestsMetrics
                );
            }

            case "DELIVERIES", "DELIVERY" -> {
                String deliveriesMetrics = fetchScope("DELIVERIES", generatedBy);
                contentsJSON = String.format(
                    "{ \"period\": \"%s to %s\", \"scope\": \"DELIVERIES\", " +
                    "\"generatedBy\": \"%s\", \"deliveries\": %s }",
                    start, end, generatedBy, deliveriesMetrics
                );
            }

            case "ALL" -> {
                String requestsMetrics   = fetchScope("REQUESTS", generatedBy);
                String deliveriesMetrics = fetchScope("DELIVERIES", generatedBy);
                contentsJSON = String.format(
                    "{ \"period\": \"%s to %s\", \"scope\": \"ALL\", " +
                    "\"generatedBy\": \"%s\", \"requests\": %s, \"deliveries\": %s }",
                    start, end, generatedBy, requestsMetrics, deliveriesMetrics
                );
            }

            default -> throw new InvalidRequestException(
                "Invalid scope: \"" + scope + "\". Allowed values: REQUESTS, DELIVERIES, ALL"
            );
        }

        long timestamp = System.currentTimeMillis();
        String packageUri = "compliance/archives/package_" + scope.toLowerCase() + "_" + timestamp + ".zip";
        String checksum   = generateChecksum(contentsJSON);

        AuditPackageDTO dto = new AuditPackageDTO();
        dto.setPeriodStart(start);
        dto.setPeriodEnd(end);
        dto.setContentsJSON(contentsJSON);
        dto.setPackageUri(packageUri);
        dto.setChecksum(checksum);
        dto.setGeneratedAt(LocalDateTime.now());

        return this.createPackage(dto);
    }

    // ─────────────────────────────────────────────
    // PRIVATE — fetch one scope via ReportService
    // ─────────────────────────────────────────────
    private String fetchScope(String scope, String generatedBy) {
        try {
            ReportDTO report = reportService.generateReportByScope(scope, generatedBy);
            return report.getMetricsJSON();
        } catch (Exception e) {
            return "{ \"error\": \"" + scope + " service unavailable: " + e.getMessage() + "\" }";
        }
    }

    // ─────────────────────────────────────────────
    // CRUD WITH RETRY
    // ─────────────────────────────────────────────

    @Transactional
    @Retryable(
        value = { Exception.class },
        maxAttemptsExpression = "${report.retry.maxAttempts}",
        backoff = @Backoff(delayExpression = "${report.retry.delay}")
    )
    public AuditPackageDTO createPackage(AuditPackageDTO dto) {
        AuditPackage entity = auditPackageMapper.toEntity(dto);
        return auditPackageMapper.toDTO(auditRepository.save(entity));
    }

    @Transactional
    @Retryable(
        value = { Exception.class },
        maxAttemptsExpression = "${report.retry.maxAttempts}",
        backoff = @Backoff(delayExpression = "${report.retry.delay}")
    )
    public AuditPackageDTO updatePackage(Long id, AuditPackageDTO dto) {
        AuditPackage existing = auditRepository.findById(id)
                .orElseThrow(() -> new InvalidRequestException(
                    "Audit Package not found with id: " + id));

        existing.setPeriodStart(dto.getPeriodStart());
        existing.setPeriodEnd(dto.getPeriodEnd());
        existing.setContentsJSON(dto.getContentsJSON());
        existing.setPackageUri(dto.getPackageUri());
        existing.setChecksum(dto.getChecksum());

        return auditPackageMapper.toDTO(auditRepository.save(existing));
    }

    public List<AuditPackageDTO> getAllPackages() {
        return auditRepository.findAll()
                .stream()
                .map(auditPackageMapper::toDTO)
                .collect(Collectors.toList());
    }

    public AuditPackageDTO getPackageById(Long id) {
        AuditPackage ap = auditRepository.findById(id)
                .orElseThrow(() -> new InvalidRequestException(
                    "Audit Package not found with id: " + id));
        return auditPackageMapper.toDTO(ap);
    }

    public List<AuditPackageDTO> getPackagesByPeriod(LocalDateTime start, LocalDateTime end) {
        return auditRepository.findByPeriod(start, end)
                .stream()
                .map(auditPackageMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePackage(Long id) {
        AuditPackage existing = auditRepository.findById(id)
                .orElseThrow(() -> new InvalidRequestException(
                    "Cannot delete. Audit Package not found with id: " + id));
        auditRepository.delete(existing);
    }

    // ─────────────────────────────────────────────
    // RECOVER
    // ─────────────────────────────────────────────

    @Recover
    public AuditPackageDTO recoverCreate(Exception e, AuditPackageDTO dto) {
        System.err.println("Retry failed during audit package creation: " + e.getMessage());
        return null;
    }

    @Recover
    public AuditPackageDTO recoverUpdate(Exception e, Long id, AuditPackageDTO dto) {
        System.err.println("Retry failed during update for id " + id + ": " + e.getMessage());
        return null;
    }

    // ─────────────────────────────────────────────
    // PRIVATE — SHA-256 checksum for tamper evidence
    // ─────────────────────────────────────────────
    private String generateChecksum(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return "checksum-unavailable";
        }
    }
}