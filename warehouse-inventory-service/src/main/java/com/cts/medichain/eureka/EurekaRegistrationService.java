package com.cts.medichain.eureka;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class EurekaRegistrationService {

    @Value("${eureka.server.url:http://localhost:8761/eureka}")
    private String eurekaUrl;

    @Value("${server.port:8082}")
    private int port;

    private static final String APP_NAME    = "WAREHOUSE-INVENTORY-SERVICE";
    private static final String INSTANCE_ID = "localhost:warehouse-inventory-service:8082";

    private final RestClient restClient = RestClient.create();

    @PostConstruct
    public void register() {
        String body = """
            {
              "instance": {
                "instanceId": "%s",
                "hostName": "localhost",
                "app": "%s",
                "ipAddr": "127.0.0.1",
                "status": "UP",
                "port": {"$": %d, "@enabled": "true"},
                "securePort": {"$": 443, "@enabled": "false"},
                "healthCheckUrl": "http://localhost:%d/actuator/health",
                "statusPageUrl": "http://localhost:%d/actuator/info",
                "homePageUrl": "http://localhost:%d/",
                "dataCenterInfo": {
                  "@class": "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo",
                  "name": "MyOwn"
                },
                "leaseInfo": {
                  "renewalIntervalInSecs": 30,
                  "durationInSecs": 90
                },
                "metadata": {}
              }
            }
            """.formatted(INSTANCE_ID, APP_NAME, port, port, port, port);

        try {
            restClient.post()
                    .uri(eurekaUrl + "/apps/" + APP_NAME)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            System.out.println("SUCCESS: Registered with Eureka as " + APP_NAME);
        } catch (Exception e) {
            System.err.println("WARNING: Could not register with Eureka - " + e.getMessage());
        }
    }

    // Send heartbeat every 30 seconds so Eureka keeps us as UP
    @Scheduled(fixedDelay = 30000)
    public void sendHeartbeat() {
        try {
            restClient.put()
                    .uri(eurekaUrl + "/apps/" + APP_NAME + "/" + INSTANCE_ID + "?status=UP")
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            System.err.println("WARNING: Eureka heartbeat failed - " + e.getMessage());
        }
    }

    // Cleanly remove from Eureka when service shuts down
    @PreDestroy
    public void deregister() {
        try {
            restClient.delete()
                    .uri(eurekaUrl + "/apps/" + APP_NAME + "/" + INSTANCE_ID)
                    .retrieve()
                    .toBodilessEntity();
            System.out.println("SUCCESS: Deregistered from Eureka");
        } catch (Exception e) {
            System.err.println("WARNING: Could not deregister from Eureka - " + e.getMessage());
        }
    }
}