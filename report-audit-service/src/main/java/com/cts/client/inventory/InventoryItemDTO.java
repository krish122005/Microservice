package com.cts.client.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemDTO {
    private Long itemId;
    private Long warehouseId;
    private Long productId;
    private Integer quantity;
    private String receivedAt;
    private String status;      
}