package com.cts.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "departmentrequest-service", fallback = KPIRequestClientFallback.class)
public interface KPIRequestClient {

    @GetMapping("/api/department-requests/stats/fulfillment")
    JsonNode getFulfillmentMetrics();
}