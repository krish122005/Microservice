package com.cts.medichain.controller;

import com.cts.medichain.entity.InventoryItem;
import com.cts.medichain.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PreAuthorize("hasAnyRole('Admin', 'head')")
    @PostMapping("/create")
    public ResponseEntity<InventoryItem> createInventory(
            @RequestBody InventoryItem inventoryItem) {
        return ResponseEntity.ok(inventoryService.createInventory(inventoryItem));
    }

    @PreAuthorize("hasAnyRole('Admin', 'head', 'operator')")
    @GetMapping("/list")
    public ResponseEntity<List<InventoryItem>> listInventory(
            @RequestParam Long warehouseId) {
        return ResponseEntity.ok(inventoryService.listInventory(warehouseId));
    }
}