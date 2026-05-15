package com.cts.medichain.controller;

import com.cts.medichain.entity.Warehouse;
import com.cts.medichain.service.WarehouseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/warehouse")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @PreAuthorize("hasRole('Admin')")
    @PostMapping("/create")
    public ResponseEntity<Warehouse> createWarehouse(
            @RequestBody Warehouse warehouse) {
        return ResponseEntity.ok(warehouseService.createWarehouse(warehouse));
    }

    @GetMapping("/list")
    public ResponseEntity<List<Warehouse>> listWarehouses() {
        return ResponseEntity.ok(warehouseService.listWarehouses());
    }
}