package com.cts.service;

import org.springframework.stereotype.Service;

import com.cts.entity.Invoice;
import com.cts.exception.InvalidRequestException;
import com.cts.feign.ProductCatalogClient;
import com.cts.repository.InvoiceRepository;

import java.util.List;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepo;
    private final ProductCatalogClient productCatalogClient; // ← injected

    public InvoiceService(InvoiceRepository invoiceRepo,
                          ProductCatalogClient productCatalogClient) {
        this.invoiceRepo = invoiceRepo;
        this.productCatalogClient = productCatalogClient;
    }

    public Invoice createInvoice(Invoice invoice) {
        // ── Your original validation logic, untouched ──
        if (invoice.getAmount() == null || invoice.getAmount() <= 0) {
            throw new InvalidRequestException("Invoice amount must be > 0");
        }
        if (invoice.getStatus() == null || invoice.getStatus().trim().isEmpty()) {
            invoice.setStatus("UNPAID");
        }

        // ── Optional: validate requestId exists in product-catalog-service ──
        // Uncomment this once your product catalog /api/orders/{id} is ready
        // productCatalogClient.getOrderById(invoice.getRequestId());

        return invoiceRepo.save(invoice);
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepo.findAll(); // your logic
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceRepo.findById(id)
                .orElseThrow(() ->
                        new InvalidRequestException("Invoice not found")); // your logic
    }
}