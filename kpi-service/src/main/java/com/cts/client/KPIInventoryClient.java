package com.cts.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.fasterxml.jackson.databind.JsonNode;

@FeignClient(name = "inventory-service")
public interface KPIInventoryClient {
    @GetMapping("/api/inventory/stats/utilization")
    JsonNode getStockUtilizationData();
}
