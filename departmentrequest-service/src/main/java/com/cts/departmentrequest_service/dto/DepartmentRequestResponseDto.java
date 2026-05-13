package com.cts.departmentrequest_service.dto;
import com.cts.departmentrequest_service.dto.DepartmentDto;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DepartmentRequestResponseDto {

	private DepartmentDto department;
    private Integer requestId;
    private Integer departmentId;
    private String productIdsJson;
    private Integer quantity;
    private String status;
    private LocalDateTime requestedAt;
    private String approvedBy;
	public Integer getRequestId() {
		return requestId;
	}
	public void setRequestId(Integer requestId) {
		this.requestId = requestId;
	}
	public Integer getDepartmentId() {
		return departmentId;
	}
	public void setDepartmentId(Integer departmentId) {
		this.departmentId = departmentId;
	}
	public String getProductIdsJson() {
		return productIdsJson;
	}
	public void setProductIdsJson(String productIdsJson) {
		this.productIdsJson = productIdsJson;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public LocalDateTime getRequestedAt() {
		return requestedAt;
	}
	public void setRequestedAt(LocalDateTime requestedAt) {
		this.requestedAt = requestedAt;
	}
	public String getApprovedBy() {
		return approvedBy;
	}
	public void setApprovedBy(String approvedBy) {
		this.approvedBy = approvedBy;
	}
	public DepartmentDto getDepartment() {
	    return department;
	}

	public void setDepartment(DepartmentDto department) {
	    this.department = department;
	}

   
}
