package com.cts.controller;


import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.cts.entity.Payment;
import com.cts.service.PaymentService;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    //@PreAuthorize("hasAnyRole('Admin', 'FINANCE')")
    @PostMapping
    public Payment recordPayment(@Valid @RequestBody Payment payment) {
        return paymentService.processPayment(payment); // your logic
    }

    //@PreAuthorize("hasAnyRole('Admin','FINANCE')")
    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments(); // your logic
    }
}
