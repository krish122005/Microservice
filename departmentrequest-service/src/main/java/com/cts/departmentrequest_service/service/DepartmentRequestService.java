package com.cts.departmentrequest_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cts.departmentrequest_service.dto.DepartmentDto;
import com.cts.departmentrequest_service.dto.DepartmentRequestCreateDto;
import com.cts.departmentrequest_service.dto.DepartmentRequestResponseDto;
import com.cts.departmentrequest_service.entity.DepartmentRequest;
import com.cts.departmentrequest_service.exception.InvalidRequestException;
import com.cts.departmentrequest_service.repository.DepartmentRequestRepository;


@Service
public class DepartmentRequestService {

    private final DepartmentRequestRepository repository;
    private final DepartmentClient departmentClient;

    public DepartmentRequestService(DepartmentRequestRepository repository,DepartmentClient departmentClient) {
        this.repository = repository;
        this.departmentClient = departmentClient;
    }
    
    //  CREATE REQUEST
    public void createRequest(DepartmentRequestCreateDto dto) {

        
        if (dto.getQuantity() <= 0) {
            throw new InvalidRequestException("Quantity must be > 0");
        }

        if (dto.getProductIds() == null || dto.getProductIds().isEmpty()) {
            throw new InvalidRequestException("Product list cannot be empty");
        }

        boolean exists =
                repository.existsByDepartmentIdAndStatus(
                        dto.getDepartmentId(), "PENDING");

        if (exists) {
            throw new InvalidRequestException(
                    "Pending request already exists for department");
        }

        String productIdsJson = dto.getProductIds().toString();

        DepartmentRequest request = DepartmentRequest.builder()
                .departmentId(dto.getDepartmentId())
                .productIdsJson(productIdsJson)
                .quantity(dto.getQuantity())
                .status("PENDING")
                .build();

        repository.save(request);
    }

    //  APPROVE REQUEST
    public void approve(Integer requestId, String username) {

        DepartmentRequest request = repository.findById(requestId)
                .orElseThrow(() ->
                        new InvalidRequestException("Request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new InvalidRequestException(
                    "Only PENDING requests can be approved");
        }

        request.setStatus("APPROVED");
        request.setApprovedBy(username);

        repository.save(request);
    }

    //  REJECT REQUEST
    public void reject(Integer requestId, String username) {

        DepartmentRequest request = repository.findById(requestId)
                .orElseThrow(() ->
                        new InvalidRequestException("Request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new InvalidRequestException(
                    "Only PENDING requests can be rejected");
        }

        request.setStatus("REJECTED");
        request.setApprovedBy(username);

        repository.save(request);
    }

    //  VIEW REQUEST
    public DepartmentRequestResponseDto view(Integer requestId) {

        DepartmentRequest request = repository.findById(requestId)
                .orElseThrow(() ->
                    new InvalidRequestException("Request not found"));

        DepartmentDto department =departmentClient.getDepartment(request.getDepartmentId());
        DepartmentRequestResponseDto dto = mapToResponse(request);
        dto.setDepartment(department);
        return dto;
    }

    //  MAPPER
    private DepartmentRequestResponseDto mapToResponse(DepartmentRequest req) {
        DepartmentRequestResponseDto dto = new DepartmentRequestResponseDto();
        dto.setRequestId(req.getRequestId());
        dto.setDepartmentId(req.getDepartmentId());
        dto.setProductIdsJson(req.getProductIdsJson());
        dto.setQuantity(req.getQuantity());
        dto.setStatus(req.getStatus());
        dto.setRequestedAt(req.getRequestedAt());
        dto.setApprovedBy(req.getApprovedBy());
        return dto;
    }

	public List<DepartmentRequestResponseDto> getAllRequests() {
    return repository.findAll()
            .stream()
            .map(this::mapToResponse)
            .collect(java.util.stream.Collectors.toList());
}

}