package com.cts.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "delivery-service")
public interface KPIDeliveryClient {

    @GetMapping("/api/deliveries/stats/completion")
    JsonNode getCompletionStats();
}