package com.cts.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.cts.client.billing.BillingIntegrationService;
import com.cts.client.billing.InvoiceDTO;
import com.cts.client.billing.PaymentDTO;
import com.cts.client.departmentRequest.DepartmentRequestDTO;
import com.cts.client.departmentRequest.DepartmentRequestIntegrationService;
import com.cts.client.delivery.DeliveryIntegrationService;
import com.cts.client.delivery.DeliveryRecordDTO;
import com.cts.client.inventory.InventoryIntegrationService;   // FIX: newly active
import com.cts.client.inventory.InventoryItemDTO;               // FIX: newly active
import com.cts.dto.ReportDTO;
import com.cts.entity.Report;
import com.cts.exception.InvalidRequestException;
import com.cts.mapper.ReportMapper;
import com.cts.repository.ReportRepository;

import jakarta.transaction.Transactional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;
    private final DepartmentRequestIntegrationService requestIntegration;
    private final DeliveryIntegrationService deliveryIntegration;
    private final BillingIntegrationService billingIntegration;
    private final InventoryIntegrationService inventoryIntegration; // FIX: injected

    @Autowired
    public ReportService(ReportRepository reportRepository,
                         ReportMapper reportMapper,
                         DepartmentRequestIntegrationService requestIntegration,
                         DeliveryIntegrationService deliveryIntegration,
                         BillingIntegrationService billingIntegration,
                         InventoryIntegrationService inventoryIntegration) {
        this.reportRepository     = reportRepository;
        this.reportMapper         = reportMapper;
        this.requestIntegration   = requestIntegration;
        this.deliveryIntegration  = deliveryIntegration;
        this.billingIntegration   = billingIntegration;
        this.inventoryIntegration = inventoryIntegration;
    }

    public ReportDTO generateReport(String scope, String type,
                                    String status, Integer id,
                                    String generatedBy) {
        return switch (scope.toUpperCase()) {
            case "REQUESTS"              -> handleRequests(type, status, id, generatedBy);
            case "DELIVERIES", "DELIVERY" -> handleDeliveries(type, status, id, generatedBy);
            case "BILLING"               -> handleBilling(type, id, generatedBy);
            case "INVENTORY"             -> handleInventory(type, id, generatedBy); // FIX: new
            default -> throw new InvalidRequestException(
                "Invalid scope: \"" + scope + "\". Allowed: REQUESTS, DELIVERIES, BILLING, INVENTORY"
            );
        };
    }


    private ReportDTO handleRequests(String type, String status, Integer id, String generatedBy) {
        return switch (type.toUpperCase()) {
            case "ALL" -> {
                List<DepartmentRequestDTO> all = requestIntegration.getAllRemoteRequests();
                long total    = all.size();
                long pending  = all.stream().filter(r -> "PENDING".equalsIgnoreCase(r.getStatus())).count();
                long approved = all.stream().filter(r -> "APPROVED".equalsIgnoreCase(r.getStatus())).count();
                long rejected = all.stream().filter(r -> "REJECTED".equalsIgnoreCase(r.getStatus())).count();
                String metrics = String.format(
                    "{ \"reportType\": \"ALL_REQUESTS\", \"totalRequests\": %d, " +
                    "\"pending\": %d, \"approved\": %d, \"rejected\": %d }",
                    total, pending, approved, rejected);
                yield buildAndSaveReport("REQUESTS", metrics, "requests_all", generatedBy);
            }
            case "STATUS" -> {
                if (status == null || status.isBlank())
                    throw new InvalidRequestException("status is required when type=STATUS");
                List<DepartmentRequestDTO> all = requestIntegration.getAllRemoteRequests();
                List<DepartmentRequestDTO> filtered = all.stream()
                    .filter(r -> status.equalsIgnoreCase(r.getStatus()))
                    .collect(Collectors.toList());
                String ids = filtered.stream()
                    .map(r -> String.valueOf(r.getRequestId()))
                    .collect(Collectors.joining(", "));
                String metrics = String.format(
                    "{ \"reportType\": \"REQUESTS_BY_STATUS\", \"statusFilter\": \"%s\", " +
                    "\"matchingCount\": %d, \"requestIds\": [%s] }",
                    status.toUpperCase(), filtered.size(), ids);
                yield buildAndSaveReport("REQUESTS", metrics, "requests_status_" + status, generatedBy);
            }
            case "ID" -> {
                if (id == null) throw new InvalidRequestException("id is required when type=ID");
                DepartmentRequestDTO req = requestIntegration.getRemoteRequestData(id);
                String metrics = String.format(
                    "{ \"reportType\": \"SINGLE_REQUEST\", \"requestId\": %d, " +
                    "\"departmentId\": %d, \"quantity\": %d, " +
                    "\"status\": \"%s\", \"approvedBy\": \"%s\" }",
                    id, req.getDepartmentId(), req.getQuantity(),
                    req.getStatus(), req.getApprovedBy() != null ? req.getApprovedBy() : "N/A");
                yield buildAndSaveReport("REQUESTS", metrics, "requests_id_" + id, generatedBy);
            }
            default -> throw new InvalidRequestException("Invalid type for REQUESTS. Allowed: ALL, STATUS, ID");
        };
    }


    private ReportDTO handleDeliveries(String type, String status, Integer id, String generatedBy) {
        return switch (type.toUpperCase()) {
            case "ALL" -> {
                List<DeliveryRecordDTO> all = deliveryIntegration.getAllDeliveries();
                long delivered = all.stream().filter(d -> "DELIVERED".equalsIgnoreCase(d.getStatus())).count();
                long closed    = all.stream().filter(d -> "CLOSED".equalsIgnoreCase(d.getStatus())).count();
                String metrics = String.format(
                    "{ \"reportType\": \"ALL_DELIVERIES\", \"totalDeliveries\": %d, " +
                    "\"delivered\": %d, \"closed\": %d }",
                    all.size(), delivered, closed);
                yield buildAndSaveReport("DELIVERIES", metrics, "deliveries_all", generatedBy);
            }
            case "STATUS" -> {
                if (status == null || status.isBlank())
                    throw new InvalidRequestException("status is required when type=STATUS");
                List<DeliveryRecordDTO> filtered = deliveryIntegration.getAllDeliveries().stream()
                    .filter(d -> status.equalsIgnoreCase(d.getStatus()))
                    .collect(Collectors.toList());
                String ids = filtered.stream()
                    .map(d -> String.valueOf(d.getDeliveryId()))
                    .collect(Collectors.joining(", "));
                String metrics = String.format(
                    "{ \"reportType\": \"DELIVERIES_BY_STATUS\", \"statusFilter\": \"%s\", " +
                    "\"matchingCount\": %d, \"deliveryIds\": [%s] }",
                    status.toUpperCase(), filtered.size(), ids);
                yield buildAndSaveReport("DELIVERIES", metrics, "deliveries_status_" + status, generatedBy);
            }
            case "ID" -> {
                if (id == null) throw new InvalidRequestException("id is required when type=ID");
                DeliveryRecordDTO d = deliveryIntegration.getDeliveryById(id);
                String metrics = String.format(
                    "{ \"reportType\": \"SINGLE_DELIVERY\", \"deliveryId\": %d, " +
                    "\"requestId\": %d, \"quantity\": %d, " +
                    "\"status\": \"%s\", \"deliveredBy\": \"%s\", \"deliveredAt\": \"%s\" }",
                    id, d.getRequestId(), d.getQuantity(),
                    d.getStatus(), d.getDeliveredBy(), d.getDeliveredAt());
                yield buildAndSaveReport("DELIVERIES", metrics, "deliveries_id_" + id, generatedBy);
            }
            default -> throw new InvalidRequestException("Invalid type for DELIVERIES. Allowed: ALL, STATUS, ID");
        };
    }


    private ReportDTO handleBilling(String type, Integer id, String generatedBy) {
        return switch (type.toUpperCase()) {
            case "ALL" -> {
                List<InvoiceDTO> invoices = billingIntegration.getAllInvoices();
                List<PaymentDTO> payments = billingIntegration.getAllPayments();
                long paid   = invoices.stream().filter(i -> "PAID".equalsIgnoreCase(i.getStatus())).count();
                long unpaid = invoices.stream().filter(i -> "UNPAID".equalsIgnoreCase(i.getStatus())).count();
                double totalAmt     = invoices.stream().mapToDouble(i -> i.getAmount() != null ? i.getAmount() : 0.0).sum();
                double collectedAmt = payments.stream().mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0).sum();
                String metrics = String.format(
                    "{ \"reportType\": \"BILLING_SUMMARY\", " +
                    "\"totalInvoices\": %d, \"paid\": %d, \"unpaid\": %d, " +
                    "\"totalInvoiceAmount\": %.2f, \"totalPayments\": %d, \"totalCollectedAmount\": %.2f }",
                    invoices.size(), paid, unpaid, totalAmt, payments.size(), collectedAmt);
                yield buildAndSaveReport("BILLING", metrics, "billing_all", generatedBy);
            }
            case "ID" -> {
                if (id == null) throw new InvalidRequestException("id is required when type=ID");
                InvoiceDTO inv = billingIntegration.getInvoiceById(Long.valueOf(id));
                if (inv == null) throw new InvalidRequestException("Invoice not found: " + id);
                String metrics = String.format(
                    "{ \"reportType\": \"SINGLE_INVOICE\", \"invoiceId\": %d, " +
                    "\"requestId\": %d, \"departmentId\": %d, " +
                    "\"amount\": %.2f, \"status\": \"%s\" }",
                    inv.getInvoiceId(), inv.getRequestId(),
                    inv.getDepartmentId(), inv.getAmount(), inv.getStatus());
                yield buildAndSaveReport("BILLING", metrics, "billing_id_" + id, generatedBy);
            }
            default -> throw new InvalidRequestException("Invalid type for BILLING. Allowed: ALL, ID");
        };
    }


    private ReportDTO handleInventory(String type, Integer id, String generatedBy) {
        return switch (type.toUpperCase()) {
            case "ALL" -> {
                List<InventoryItemDTO> all = inventoryIntegration.getAllInventory();
                long available  = all.stream().filter(i -> "AVAILABLE".equalsIgnoreCase(i.getStatus())).count();
                long dispatched = all.stream().filter(i -> "DISPATCHED".equalsIgnoreCase(i.getStatus())).count();
                int  totalQty   = all.stream().mapToInt(i -> i.getQuantity() != null ? i.getQuantity() : 0).sum();
                String metrics = String.format(
                    "{ \"reportType\": \"INVENTORY_SUMMARY\", \"totalItems\": %d, " +
                    "\"available\": %d, \"dispatched\": %d, \"totalQuantity\": %d }",
                    all.size(), available, dispatched, totalQty);
                yield buildAndSaveReport("INVENTORY", metrics, "inventory_all", generatedBy);
            }
            case "WAREHOUSE" -> {
                if (id == null) throw new InvalidRequestException("id (warehouseId) is required when type=WAREHOUSE");
                List<InventoryItemDTO> items = inventoryIntegration.getStockByWarehouse(Long.valueOf(id));
                int totalQty = items.stream().mapToInt(i -> i.getQuantity() != null ? i.getQuantity() : 0).sum();
                String metrics = String.format(
                    "{ \"reportType\": \"INVENTORY_BY_WAREHOUSE\", \"warehouseId\": %d, " +
                    "\"itemCount\": %d, \"totalQuantity\": %d }",
                    id, items.size(), totalQty);
                yield buildAndSaveReport("INVENTORY", metrics, "inventory_warehouse_" + id, generatedBy);
            }
            default -> throw new InvalidRequestException("Invalid type for INVENTORY. Allowed: ALL, WAREHOUSE");
        };
    }


    public Report getReportById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + id));
    }

    public ReportDTO getReportDTOById(Long id) {
        return reportMapper.toDTO(getReportById(id));
    }

    public List<ReportDTO> getAllReportsDTO() {
        return reportRepository.findAll().stream()
                .map(reportMapper::toDTO)
                .collect(Collectors.toList());
    }

    public String getFileName(Long id) {
        return getReportById(id).getFileName();
    }

    public void deleteReportById(Long id) {
        if (!reportRepository.existsById(id))
            throw new RuntimeException("Report not found: " + id);
        reportRepository.deleteById(id);
    }

    @Transactional
    public void deleteReportByFileName(String fileName) {
        reportRepository.deleteByFileName(fileName);
    }

    @Retryable(
        value = {Exception.class},
        maxAttemptsExpression = "${report.retry.maxAttempts}",
        backoff = @Backoff(delayExpression = "${report.retry.delay}")
    )
    public ReportDTO createReport(ReportDTO reportDTO) {
        if (reportDTO.getFileName() == null || reportDTO.getFileName().isEmpty())
            throw new IllegalArgumentException("File name cannot be empty for compliance.");
        return reportMapper.toDTO(reportRepository.save(reportMapper.toEntity(reportDTO)));
    }

    @Recover
    public ReportDTO recoverCreateReport(Exception e, ReportDTO reportDTO) {
        System.err.println("Database retry failed for Report: " + reportDTO.getFileName());
        return null;
    }

    public ReportDTO generateReportByScope(String scope, String generatedBy) {
        return generateReport(scope, "ALL", null, null, generatedBy);
    }

    private ReportDTO buildAndSaveReport(String scope, String metrics,
                                         String filePrefix, String generatedBy) {
        long ts       = System.currentTimeMillis();
        String fileName = filePrefix.toLowerCase() + "_" + ts + ".pdf";
        ReportDTO dto = new ReportDTO();
        dto.setScope(scope);
        dto.setGeneratedBy(generatedBy);
        dto.setGeneratedAt(LocalDateTime.now());
        dto.setFileName(fileName);
        dto.setReportUri("reports/" + fileName);
        dto.setParametersJSON("{ \"scope\": \"" + scope + "\", \"generatedBy\": \"" + generatedBy + "\" }");
        dto.setMetricsJSON(metrics);
        return this.createReport(dto);
    }
}