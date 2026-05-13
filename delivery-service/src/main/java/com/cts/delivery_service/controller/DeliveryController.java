package com.cts.delivery_service.controller;

import java.util.List;

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

    // CREATE DELIVERY
    @PostMapping
    public String createDelivery(@RequestBody DeliveryCreateDto dto) {
        service.createDelivery(dto);
        return "Delivery record created";
    }

    // LIST DELIVERIES
    @GetMapping
    public List<DeliveryResponseDto> listDeliveries(
            @RequestParam(required = false) Integer requestId) {

        return service.listDeliveries(requestId);
    }

    // CLOSE DELIVERY
    @PutMapping("/{deliveryId}/close")
    public String closeRequest(@PathVariable Integer deliveryId) {
        service.closeRequest(deliveryId);
        return "Request closed successfully";
    }
    
 // ADD THIS — needed by Feign client in report-audit-service
    @GetMapping("/{deliveryId}")
    public DeliveryResponseDto getDeliveryById(@PathVariable Integer deliveryId) {
        return service.getDeliveryById(deliveryId);
    }
}