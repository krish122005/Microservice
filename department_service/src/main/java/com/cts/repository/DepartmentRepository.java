package com.cts.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cts.model.department;

public interface DepartmentRepository extends JpaRepository<department, Long> {

	
}
