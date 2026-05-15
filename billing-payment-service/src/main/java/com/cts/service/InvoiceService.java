package com.cts.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cts.entity.Invoice;
import com.cts.exception.InvalidRequestException;
import com.cts.feign.DepartmentRequestDTO;
import com.cts.feign.DepartmentRequestValidationClient;
import com.cts.repository.InvoiceRepository;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepo;

    private final DepartmentRequestValidationClient requestValidationClient;

    public InvoiceService(InvoiceRepository invoiceRepo,
                          DepartmentRequestValidationClient requestValidationClient) {
        this.invoiceRepo             = invoiceRepo;
        this.requestValidationClient = requestValidationClient;
    }

    public Invoice createInvoice(Invoice invoice) {
        if (invoice.getAmount() == null || invoice.getAmount() <= 0) {
            throw new InvalidRequestException("Invoice amount must be > 0");
        }
        if (invoice.getStatus() == null || invoice.getStatus().trim().isEmpty()) {
            invoice.setStatus("UNPAID");
        }

        // FIX: validation is now active (was commented out).
        // We call departmentrequest-service to confirm the request exists
        // and is in APPROVED status before allowing an invoice to be raised.
        if (invoice.getRequestId() != null) {
            DepartmentRequestDTO request =
                requestValidationClient.getRequestById(invoice.getRequestId());

            if (request == null) {
                // Fallback was triggered — departmentrequest-service is down.
                // Fail safe: reject the invoice rather than creating orphans.
                throw new InvalidRequestException(
                    "Cannot create invoice: departmentrequest-service is unavailable. " +
                    "Try again shortly."
                );
            }

            if (!"APPROVED".equalsIgnoreCase(request.getStatus())) {
                throw new InvalidRequestException(
                    "Cannot create invoice: request " + invoice.getRequestId() +
                    " is not APPROVED (current status: " + request.getStatus() + ")"
                );
            }
        }

        return invoiceRepo.save(invoice);
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepo.findAll();
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceRepo.findById(id)
                .orElseThrow(() -> new InvalidRequestException("Invoice not found"));
    }
}