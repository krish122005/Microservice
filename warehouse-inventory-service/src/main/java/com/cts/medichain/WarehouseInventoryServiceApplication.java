package com.cts.medichain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class WarehouseInventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WarehouseInventoryServiceApplication.class, args);
    }
}