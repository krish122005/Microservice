package com.cts.departmentrequest_service.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cts.departmentrequest_service.dto.DepartmentRequestCreateDto;
import com.cts.departmentrequest_service.dto.DepartmentRequestResponseDto;
import com.cts.departmentrequest_service.service.DepartmentRequestService;

@RestController
@RequestMapping("/api/department-requests")
public class DepartmenRequestController {

    private final DepartmentRequestService service;

    public DepartmenRequestController(DepartmentRequestService service) {
        this.service = service;
    }

    // CREATE REQUEST
    @PostMapping
    public String createRequest(@RequestBody DepartmentRequestCreateDto dto) {
        service.createRequest(dto);
        return "Department request created successfully";
    }

    // APPROVE REQUEST
    @PutMapping("/{requestId}/approve")
    public String approveRequest(@PathVariable Integer requestId) {
        service.approve(requestId, "SYSTEM");
        return "Request approved";
    }

    // REJECT REQUEST
    @PutMapping("/{requestId}/reject")
    public String rejectRequest(@PathVariable Integer requestId) {
        service.reject(requestId, "SYSTEM");
        return "Request rejected";
    }

    // VIEW REQUEST
    @GetMapping("/{requestId}")
    public DepartmentRequestResponseDto viewRequest(
            @PathVariable Integer requestId) {
        return service.view(requestId);
    }
    
    @GetMapping
    public List<DepartmentRequestResponseDto> getAllRequests() {
        return service.getAllRequests();
    }
}