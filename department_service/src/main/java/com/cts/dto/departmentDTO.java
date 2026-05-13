package com.cts.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class departmentDTO {
	
	Long id;
	String departmentName;
	String description;
	String departmentCode;

}
