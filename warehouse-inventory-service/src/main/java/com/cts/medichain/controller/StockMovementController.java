package com.cts.medichain.controller;

import com.cts.medichain.entity.StockMovement;
import com.cts.medichain.service.StockMovementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/stock")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    public StockMovementController(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    @PreAuthorize("hasRole('operator')")
    @PostMapping("/move")
    public ResponseEntity<String> createStockMovement(
            @RequestBody StockMovement movement) {
        stockMovementService.createMovement(movement);
        return ResponseEntity.ok("Stock moved successfully");
    }

    @PreAuthorize("hasAnyRole('operator', 'auditor')")
    @GetMapping("/movements")
    public ResponseEntity<List<StockMovement>> listStockMovements() {
        return ResponseEntity.ok(stockMovementService.listMovements());
    }
}