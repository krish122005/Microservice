package com.cts.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "warehouse-inventory-service")
public interface KPIInventoryClient {

    @GetMapping("/api/inventory/stats/utilization")
    JsonNode getStockUtilizationData();
}