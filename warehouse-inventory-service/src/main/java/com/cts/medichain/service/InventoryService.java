package com.cts.medichain.service;

import com.cts.medichain.entity.InventoryItem;
import com.cts.medichain.entity.Warehouse;
import com.cts.medichain.repository.InventoryRepository;
import com.cts.medichain.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final AuditLogService auditLogService;

    public InventoryService(InventoryRepository inventoryRepository,
                            WarehouseRepository warehouseRepository,
                            AuditLogService auditLogService) {
        this.inventoryRepository = inventoryRepository;
        this.warehouseRepository = warehouseRepository;
        this.auditLogService = auditLogService;
    }

    public InventoryItem createInventory(InventoryItem inventoryItem) {

        if (inventoryItem.getQuantity() <= 0)
            throw new RuntimeException("Quantity must be greater than zero");

        Long warehouseId = inventoryItem.getWarehouse().getWarehouseId();
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        inventoryItem.setWarehouse(warehouse);
        inventoryItem.setStatus("AVAILABLE");

        InventoryItem saved = inventoryRepository.save(inventoryItem);

        auditLogService.logAction(
                "CREATE_INVENTORY", "INVENTORY",
                saved.getItemId(),
                "Inventory created for productId=" + saved.getProductId()
                + ", warehouseId=" + warehouse.getWarehouseId()
                + ", quantity=" + saved.getQuantity());

        return saved;
    }

    public List<InventoryItem> listInventory(Long warehouseId) {
        return inventoryRepository.findByWarehouse_WarehouseId(warehouseId);
    }
}