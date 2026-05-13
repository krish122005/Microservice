package com.cts.service;

import org.springframework.stereotype.Service;

import com.cts.dto.departmentDTO;
@Service
public interface departmentService {
	departmentDTO saveEmployee(departmentDTO departmentdto);
	departmentDTO getEmployeeById(Long departmentId);

}
