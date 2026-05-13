package com.cts.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cts.dto.departmentDTO;
import com.cts.model.department;
@Component
public class departmentMapper {
	
	@Autowired
	departmentDTO departmentdto;

	public department toEntity(departmentDTO department_dto) {
		department dept=new department();
		dept.setId(department_dto.getId());
		dept.setDepartmentName(department_dto.getDepartmentName());
		dept.setDepartmentCode(department_dto.getDepartmentCode());
		dept.setDescription(department_dto.getDescription());
		
		return dept;
	}
	public departmentDTO todto(department department) {
		departmentDTO dept=new departmentDTO();
		dept.setId(department.getId());
		dept.setDepartmentName(department.getDepartmentName());
		dept.setDepartmentCode(department.getDepartmentCode());
		dept.setDescription(department.getDescription());
		
		return dept;
	}
}
