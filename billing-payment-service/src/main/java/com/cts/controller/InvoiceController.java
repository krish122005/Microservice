package com.cts.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.cts.entity.Invoice;
import com.cts.service.InvoiceService;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    //@PreAuthorize("hasRole('Admin')")
    @PostMapping
    public Invoice createInvoice(@Valid @RequestBody Invoice invoice) {
        return invoiceService.createInvoice(invoice); // your logic
    }

    //@PreAuthorize("hasAnyRole('Admin','FINANCE')")
    @GetMapping
    public List<Invoice> getAllInvoices() {
        return invoiceService.getAllInvoices(); // your logic
    }

   // @PreAuthorize("hasAnyRole('Admin','FINANCE')")
    @GetMapping("/{id}")
    public Invoice getInvoice(@PathVariable Long id) {
        return invoiceService.getInvoiceById(id); // your logic
    }
}