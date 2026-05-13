package com.cts.delivery_service.dto;

public class DeliveryCreateDto {

    private Integer requestId;
    private Integer quantity;
    
	public Integer getRequestId() {
		return requestId;
	}
	public void setRequestId(Integer requestId) {
		this.requestId = requestId;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

}