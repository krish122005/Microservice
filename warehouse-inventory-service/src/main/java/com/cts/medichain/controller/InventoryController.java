package com.cts.medichain.controller;

import com.cts.medichain.entity.InventoryItem;
import com.cts.medichain.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/create")
    public ResponseEntity<InventoryItem> createInventory(@RequestBody InventoryItem inventoryItem) {
        return ResponseEntity.ok(inventoryService.createInventory(inventoryItem));
    }

    @GetMapping("/list")
    public ResponseEntity<List<InventoryItem>> listInventory(@RequestParam Long warehouseId) {
        return ResponseEntity.ok(inventoryService.listInventory(warehouseId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<InventoryItem>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @GetMapping("/stats/utilization")
    public ResponseEntity<Map<String, Object>> getUtilizationStats() {
        List<InventoryItem> all = inventoryService.getAllInventory();
        long total      = all.size();
        long available  = all.stream().filter(i -> "AVAILABLE".equalsIgnoreCase(i.getStatus())).count();
        long dispatched = all.stream().filter(i -> "DISPATCHED".equalsIgnoreCase(i.getStatus())).count();
        double rate     = total > 0 ? Math.round((dispatched * 100.0 / total) * 100.0) / 100.0 : 0.0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalItems",      total);
        stats.put("available",       available);
        stats.put("dispatched",      dispatched);
        stats.put("utilizationRate", rate);
        return ResponseEntity.ok(stats);
    }
}