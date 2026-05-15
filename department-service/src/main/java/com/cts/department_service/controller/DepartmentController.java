package com.cts.department_service.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.cts.department_service.dto.DepartmentRequestDto;
import com.cts.department_service.dto.DepartmentResponseDto;
import com.cts.department_service.service.DepartmentService;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService service;

    public DepartmentController(DepartmentService service) {
        this.service = service;
    }

    // CREATE DEPARTMENT
    @PostMapping
    public String createDepartment(@RequestBody DepartmentRequestDto dto) {
        service.createDepartment(dto);
        return "Department created successfully";
    }

    // GET ALL
    @GetMapping
    public List<DepartmentResponseDto> getAllDepartments() {
        return service.getAllDepartments();
    }

    // GET BY ID
    @GetMapping("/{id}")
    public DepartmentResponseDto getDepartment(@PathVariable Integer id) {
        return service.getDepartmentById(id);
    }
}