package com.cts.delivery_service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.delivery_service.dto.DeliveryCreateDto;
import com.cts.delivery_service.dto.DeliveryResponseDto;
import com.cts.delivery_service.entity.DeliveryRecord;
import com.cts.delivery_service.exception.InvalidRequestException;
import com.cts.delivery_service.repository.DeliveryRecordRepository;

@Service
@Transactional
public class DeliveryService {

    private final DeliveryRecordRepository deliveryRepo;

    public DeliveryService(DeliveryRecordRepository deliveryRepo) {
        this.deliveryRepo = deliveryRepo;
    }

    // CREATE DELIVERY 
    public void createDelivery(DeliveryCreateDto dto) {

        if (dto.getQuantity() <= 0) {
            throw new InvalidRequestException("Quantity must be > 0");
        }

        // ❌ Removed cross‑service DB validation (request-service / department-service)

        if (deliveryRepo.existsByRequestId(dto.getRequestId())) {
            throw new InvalidRequestException(
                    "Delivery already exists for this request");
        }

        DeliveryRecord delivery = DeliveryRecord.builder()
                .requestId(dto.getRequestId())
                .quantity(dto.getQuantity())
                .deliveredBy("SYSTEM") // JWT later
                .status("DELIVERED")
                .build();

        deliveryRepo.save(delivery);
    }

    // LIST DELIVERIES 
    public List<DeliveryResponseDto> listDeliveries(Integer requestId) {

        List<DeliveryRecord> records =
                requestId == null
                        ? deliveryRepo.findAll()
                        : deliveryRepo.findByRequestId(requestId);

        return records.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /* ✅ CLOSE DELIVERY */
    public void closeRequest(Integer deliveryId) {

        DeliveryRecord delivery = deliveryRepo.findById(deliveryId)
                .orElseThrow(() ->
                        new InvalidRequestException("Delivery not found"));

        if (!"DELIVERED".equals(delivery.getStatus())) {
            throw new InvalidRequestException(
                    "Only DELIVERED deliveries can be closed");
        }

        delivery.setStatus("CLOSED");
        deliveryRepo.save(delivery);
    }
    
    public DeliveryResponseDto getDeliveryById(Integer deliveryId) {
        DeliveryRecord record = deliveryRepo.findById(deliveryId)
            .orElseThrow(() -> new InvalidRequestException("Delivery not found: " + deliveryId));
        return mapToResponse(record);
    }

    // MAPPER
    private DeliveryResponseDto mapToResponse(DeliveryRecord delivery) {
        DeliveryResponseDto dto = new DeliveryResponseDto();
        dto.setDeliveryId(delivery.getDeliveryId());
        dto.setRequestId(delivery.getRequestId());
        dto.setDeliveredBy(delivery.getDeliveredBy());
        dto.setDeliveredAt(delivery.getDeliveredAt());
        dto.setQuantity(delivery.getQuantity());
        dto.setStatus(delivery.getStatus());
        return dto;
    }
}