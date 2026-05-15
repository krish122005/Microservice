package com.cts.department_service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cts.department_service.dto.*;
import com.cts.department_service.entity.Department;
import com.cts.department_service.exception.InvalidRequestException;
import com.cts.department_service.repository.DepartmentRepository;

@Service
public class DepartmentService {

    private final DepartmentRepository repository;

    public DepartmentService(DepartmentRepository repository) {
        this.repository = repository;
    }

    // CREATE
    public void createDepartment(DepartmentRequestDto dto) {

        // ✅ OLD VALIDATION — preserved
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new InvalidRequestException("Department name is required");
        }

        if (repository.existsByNameIgnoreCase(dto.getName().trim())) {
            throw new InvalidRequestException("Department already exists");
        }

        Department department = Department.builder()
                .name(dto.getName().trim())
                .headId(dto.getHeadId())
                .contactInfo(dto.getContactInfo())
                .status(dto.getStatus() != null ? dto.getStatus() : "ACTIVE")
                .build();

        repository.save(department);
    }

    // GET BY ID
    public DepartmentResponseDto getDepartmentById(Integer id) {

        Department dept = repository.findById(id)
                .orElseThrow(() ->
                        new InvalidRequestException("Department not found"));

        return mapToResponse(dept);
    }

    // GET ALL
    public List<DepartmentResponseDto> getAllDepartments() {
        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // MAPPER
    private DepartmentResponseDto mapToResponse(Department dept) {
        DepartmentResponseDto dto = new DepartmentResponseDto();
        dto.setDepartmentId(dept.getDepartmentId());
        dto.setName(dept.getName());
        dto.setHeadId(dept.getHeadId());
        dto.setContactInfo(dept.getContactInfo());
        dto.setStatus(dept.getStatus());
        return dto;
    }
}
