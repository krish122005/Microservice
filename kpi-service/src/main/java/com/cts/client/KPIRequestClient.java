package com.cts.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "request-service")
public interface KPIRequestClient {
    @GetMapping("/api/requests/stats/fulfillment")
    JsonNode getFulfillmentMetrics();
}