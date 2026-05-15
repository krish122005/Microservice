package com.cts.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class KPIRequestClientFallback implements KPIRequestClient {

    private static final Logger log = LoggerFactory.getLogger(KPIRequestClientFallback.class);

    @Override
    public JsonNode getFulfillmentMetrics() {
        log.warn("departmentrequest-service unavailable — returning empty fulfillment metrics");
        ObjectNode node = new ObjectMapper().createObjectNode();
        node.put("error", "departmentrequest-service unavailable");
        node.put("totalRequests", 0);
        node.put("fulfilled", 0);
        node.put("fulfillmentRate", 0.0);
        return node;
    }
}

@Component
class KPIDeliveryClientFallback implements KPIDeliveryClient {

    private static final Logger log = LoggerFactory.getLogger(KPIDeliveryClientFallback.class);

    @Override
    public JsonNode getCompletionStats() {
        log.warn("delivery-service unavailable — returning empty completion stats");
        ObjectNode node = new ObjectMapper().createObjectNode();
        node.put("error", "delivery-service unavailable");
        node.put("totalDeliveries", 0);
        node.put("completed", 0);
        node.put("completionRate", 0.0);
        return node;
    }
}

@Component
class KPIInventoryClientFallback implements KPIInventoryClient {

    private static final Logger log = LoggerFactory.getLogger(KPIInventoryClientFallback.class);

    @Override
    public JsonNode getStockUtilizationData() {
        log.warn("warehouse-inventory-service unavailable — returning empty utilization data");
        ObjectNode node = new ObjectMapper().createObjectNode();
        node.put("error", "warehouse-inventory-service unavailable");
        node.put("totalItems", 0);
        node.put("utilizationRate", 0.0);
        return node;
    }
}