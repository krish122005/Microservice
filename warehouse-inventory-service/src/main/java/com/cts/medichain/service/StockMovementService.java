package com.cts.medichain.service;

import com.cts.medichain.entity.InventoryItem;
import com.cts.medichain.entity.StockMovement;
import com.cts.medichain.entity.Warehouse;
import com.cts.medichain.repository.InventoryRepository;
import com.cts.medichain.repository.StockMovementRepository;
import com.cts.medichain.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final AuditLogService auditLogService;

    public StockMovementService(StockMovementRepository stockMovementRepository,
                                InventoryRepository inventoryRepository,
                                WarehouseRepository warehouseRepository,
                                AuditLogService auditLogService) {
        this.stockMovementRepository = stockMovementRepository;
        this.inventoryRepository = inventoryRepository;
        this.warehouseRepository = warehouseRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public StockMovement createMovement(StockMovement movement) {

        if (movement.getQuantity() <= 0)
            throw new RuntimeException("Quantity must be positive");

        Warehouse fromWarehouse = warehouseRepository
                .findById(movement.getFromWarehouseId())
                .orElseThrow(() -> new RuntimeException("Source warehouse not found"));

        Warehouse toWarehouse = warehouseRepository
                .findById(movement.getToWarehouseId())
                .orElseThrow(() -> new RuntimeException("Destination warehouse not found"));

        InventoryItem source = inventoryRepository
                .findByWarehouse_WarehouseIdAndProductId(
                        fromWarehouse.getWarehouseId(),
                        movement.getProductId())
                .orElseThrow(() -> new RuntimeException("Source inventory not found"));

        if (source.getQuantity() < movement.getQuantity())
            throw new RuntimeException("Insufficient stock in source warehouse");

        // Deduct from source
        source.setQuantity(source.getQuantity() - movement.getQuantity());
        inventoryRepository.save(source);

        // Add to destination — create if not exists
        InventoryItem destination = inventoryRepository
                .findByWarehouse_WarehouseIdAndProductId(
                        toWarehouse.getWarehouseId(),
                        movement.getProductId())
                .orElse(null);

        if (destination == null) {
            destination = InventoryItem.builder()
                    .warehouse(toWarehouse)
                    .productId(movement.getProductId())
                    .quantity(movement.getQuantity())
                    .status("AVAILABLE")
                    .build();
        } else {
            destination.setQuantity(destination.getQuantity() + movement.getQuantity());
        }
        inventoryRepository.save(destination);

        // Save movement record
        movement.setStatus("COMPLETED");
        StockMovement saved = stockMovementRepository.save(movement);

        auditLogService.logAction(
                "MOVE_STOCK", "STOCK_MOVEMENT",
                saved.getMovementId(),
                "Moved productId=" + saved.getProductId()
                + " from warehouseId=" + fromWarehouse.getWarehouseId()
                + " to warehouseId=" + toWarehouse.getWarehouseId()
                + " quantity=" + saved.getQuantity());

        return saved;
    }

    public List<StockMovement> listMovements() {
        return stockMovementRepository.findAll();
    }
}