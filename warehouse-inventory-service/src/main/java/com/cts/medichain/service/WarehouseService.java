package com.cts.medichain.service;

import com.cts.medichain.entity.Warehouse;
import com.cts.medichain.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final AuditLogService auditLogService;

    public WarehouseService(WarehouseRepository warehouseRepository,
                            AuditLogService auditLogService) {
        this.warehouseRepository = warehouseRepository;
        this.auditLogService = auditLogService;
    }

    public Warehouse createWarehouse(Warehouse warehouse) {
        warehouse.setActive(true);
        Warehouse saved = warehouseRepository.save(warehouse);
        auditLogService.logAction(
                "CREATE_WAREHOUSE", "WAREHOUSE",
                saved.getWarehouseId(),
                "Warehouse created: " + saved.getName());
        return saved;
    }

    public List<Warehouse> listWarehouses() {
        return warehouseRepository.findAll();
    }
}