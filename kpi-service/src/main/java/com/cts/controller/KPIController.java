package com.cts.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cts.dto.KPIDTO;
import com.cts.response.ApiResponse;
import com.cts.service.KPIService;

@RestController
@RequestMapping("/api/kpi")
public class KPIController {

    @Autowired
    private KPIService kpiService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<KPIDTO>>> getAllKPIs() {
        List<KPIDTO> kpis = kpiService.getAllKPIs();
        return ResponseEntity.ok(ApiResponse.success("KPI metrics retrieved successfully", kpis));
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<KPIDTO>>> getKPIsByCategory(@RequestParam String category) {
        List<KPIDTO> filteredKpis = kpiService.getKPIsByCategory(category);
        return ResponseEntity.ok(ApiResponse.success("KPIs filtered by category: " + category, filteredKpis));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KPIDTO>> getKPIById(@PathVariable Long id) {
        KPIDTO kpi = kpiService.getKPIById(id);
        return ResponseEntity.ok(ApiResponse.success("KPI metric found", kpi));
    }
    
    @PostMapping 
    public ResponseEntity<ApiResponse<KPIDTO>> createKPI(@RequestBody KPIDTO kpiDto) {
        KPIDTO createdKpi = kpiService.createKPI(kpiDto);
        return new ResponseEntity<>(
            ApiResponse.success("New KPI metric established", createdKpi), 
            HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<KPIDTO>> updateKPI(@PathVariable Long id, @RequestBody KPIDTO kpiDto) {
        KPIDTO updatedKpi = kpiService.updateKPI(id, kpiDto);
        return ResponseEntity.ok(ApiResponse.success("KPI metric updated successfully", updatedKpi));
    }
} 