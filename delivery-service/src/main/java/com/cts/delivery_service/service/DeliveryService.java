package com.cts.delivery_service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.delivery_service.dto.DeliveryCreateDto;
import com.cts.delivery_service.dto.DeliveryResponseDto;
import com.cts.delivery_service.dto.DepartmentRequestDto;
import com.cts.delivery_service.dto.NotificationRequestDto;
import com.cts.delivery_service.entity.DeliveryRecord;
import com.cts.delivery_service.exception.InvalidRequestException;
import com.cts.delivery_service.repository.DeliveryRecordRepository;

@Service
@Transactional
public class DeliveryService {

    private final DeliveryRecordRepository deliveryRepo;
    private final DepartmentRequestClient requestClient;
    private final NotificationClient notificationClient;

    public DeliveryService(DeliveryRecordRepository deliveryRepo,
                           DepartmentRequestClient requestClient,
                           NotificationClient notificationClient) {
        this.deliveryRepo = deliveryRepo;
        this.requestClient = requestClient;
        this.notificationClient = notificationClient;
    }

    // CREATE DELIVERY
    public DeliveryResponseDto createDelivery(DeliveryCreateDto dto, String deliveredBy) {

        if (dto.getQuantity() <= 0) {
            throw new InvalidRequestException("Quantity must be > 0");
        }

        // VALIDATE request exists and is APPROVED
        DepartmentRequestDto request = requestClient.getRequest(dto.getRequestId());
        if (request == null) {
            throw new InvalidRequestException(
                    "Department request not found or service unavailable");
        }
        if (!"APPROVED".equals(request.getStatus())) {
            throw new InvalidRequestException(
                    "Only APPROVED requests can have a delivery. Current status: "
                    + request.getStatus());
        }

        // VALIDATE quantity matches approved request quantity
        if (!dto.getQuantity().equals(request.getQuantity())) {
            throw new InvalidRequestException(
                    "Delivery quantity must match approved request quantity: "
                    + request.getQuantity());
        }

        // CHECK no duplicate delivery for same request
        if (deliveryRepo.existsByRequestId(dto.getRequestId())) {
            throw new InvalidRequestException(
                    "Delivery already exists for this request");
        }

        DeliveryRecord delivery = DeliveryRecord.builder()
                .requestId(dto.getRequestId())
                .quantity(dto.getQuantity())
                .deliveredBy(deliveredBy)
                .status("DELIVERED")
                .build();

        DeliveryRecord saved = deliveryRepo.save(delivery);

        // NOTIFY — alert department HEAD using headId
        if (request.getHeadId() != null) {
            notificationClient.sendNotification(new NotificationRequestDto(
                    request.getHeadId().longValue(),
                    saved.getDeliveryId().longValue(),
                    "Your supply request #" + dto.getRequestId() +
                    " has been DELIVERED by " + deliveredBy +
                    ". Please upload proof of receipt.",
                    "DELIVERY"
            ));
        }

        return mapToResponse(saved);
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

    // CLOSE DELIVERY
    public DeliveryResponseDto closeRequest(Integer deliveryId) {

        DeliveryRecord delivery = deliveryRepo.findById(deliveryId)
                .orElseThrow(() ->
                        new InvalidRequestException("Delivery not found"));

        if (!"DELIVERED".equals(delivery.getStatus())) {
            throw new InvalidRequestException(
                    "Only DELIVERED deliveries can be closed");
        }

        delivery.setStatus("CLOSED");
        DeliveryRecord saved = deliveryRepo.save(delivery);

        // FETCH request to get headId for notification
        DepartmentRequestDto request = requestClient.getRequest(delivery.getRequestId());

        if (request != null && request.getHeadId() != null) {
            notificationClient.sendNotification(new NotificationRequestDto(
                    request.getHeadId().longValue(),
                    deliveryId.longValue(),
                    "Delivery #" + deliveryId +
                    " for request #" + delivery.getRequestId() +
                    " has been CLOSED successfully.",
                    "DELIVERY"
            ));
        }

        return mapToResponse(saved);
    }

    // GET BY ID
    public DeliveryResponseDto getDeliveryById(Integer deliveryId) {
        DeliveryRecord record = deliveryRepo.findById(deliveryId)
                .orElseThrow(() ->
                        new InvalidRequestException("Delivery not found: " + deliveryId));
        return mapToResponse(record);
    }

    // MAPPER
    private DeliveryResponseDto mapToResponse(DeliveryRecord delivery) {
        return new DeliveryResponseDto(
                delivery.getDeliveryId(),
                delivery.getRequestId(),
                delivery.getDeliveredBy(),
                delivery.getDeliveredAt(),
                delivery.getQuantity(),
                delivery.getStatus()
        );
    }
}
