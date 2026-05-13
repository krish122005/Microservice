package com.cts.departmentrequest_service.service;

import org.springframework.stereotype.Component;

import com.cts.departmentrequest_service.dto.DepartmentDto;

@Component
public class DepartmentClientFallback implements DepartmentClient {

    @Override
    public DepartmentDto getDepartment(Integer id) {
        DepartmentDto dto = new DepartmentDto();
        dto.setDepartmentId(id);
        dto.setName("Default Department");
        dto.setStatus("INACTIVE");
        dto.setContactInfo("Department Service is DOWN");
        return dto;
    }
}