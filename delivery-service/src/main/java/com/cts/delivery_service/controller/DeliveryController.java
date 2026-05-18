package com.cts.delivery_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cts.delivery_service.dto.DeliveryCreateDto;
import com.cts.delivery_service.dto.DeliveryResponseDto;
import com.cts.delivery_service.service.DeliveryService;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService service;

    public DeliveryController(DeliveryService service) {
        this.service = service;
    }

    // CREATE DELIVERY — WAREHOUSE only
    @PostMapping
    public ResponseEntity<?> createDelivery(
            @RequestBody DeliveryCreateDto dto,
            @RequestHeader("X-Auth-Role") String role,
            @RequestHeader("X-Auth-User") String deliveredBy) {

        if (!"WAREHOUSE".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Only WAREHOUSE can create deliveries.");
        }

        DeliveryResponseDto created = service.createDelivery(dto, deliveredBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // LIST DELIVERIES — ADMIN, WAREHOUSE, AUDITOR
    @GetMapping
    public ResponseEntity<?> listDeliveries(
            @RequestParam(required = false) Integer requestId,
            @RequestHeader("X-Auth-Role") String role) {

        if (!"ADMIN".equals(role) && !"WAREHOUSE".equals(role)
                && !"AUDITOR".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied.");
        }

        return ResponseEntity.ok(service.listDeliveries(requestId));
    }

    // CLOSE DELIVERY — WAREHOUSE only
    @PutMapping("/{deliveryId}/close")
    public ResponseEntity<?> closeRequest(
            @PathVariable Integer deliveryId,
            @RequestHeader("X-Auth-Role") String role) {

        if (!"WAREHOUSE".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Only WAREHOUSE can close deliveries.");
        }

        DeliveryResponseDto closed = service.closeRequest(deliveryId);
        return ResponseEntity.ok(closed);
    }

    // GET BY ID — ADMIN, WAREHOUSE, AUDITOR, DEPARTMENT_HEAD
    @GetMapping("/{deliveryId}")
    public ResponseEntity<?> getDeliveryById(
            @PathVariable Integer deliveryId,
            @RequestHeader("X-Auth-Role") String role) {

        if (!"ADMIN".equals(role) && !"WAREHOUSE".equals(role)
                && !"AUDITOR".equals(role) && !"DEPARTMENT_HEAD".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied.");
        }

        return ResponseEntity.ok(service.getDeliveryById(deliveryId));
    }
}
