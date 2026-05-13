package com.cts.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "departmentrequest-service")
public interface ProductCatalogClient {

    @GetMapping("/api/department-requests/{requestId}")
    Object getOrderById(@PathVariable("requestId") Long requestId);
}