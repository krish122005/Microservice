package com.cts.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cts.service.departmentService;

@RestController
@RequestMapping("/api/departments")
public class departmentController {

	private departmentService departmentservice;
	
	public departmentController(departmentService departmentservice) {
		super();
		this.departmentservice=departmentservice;
	}
}
