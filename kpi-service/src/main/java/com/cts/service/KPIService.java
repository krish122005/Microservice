package com.cts.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.client.KPIDeliveryClient;
import com.cts.client.KPIInventoryClient;
import com.cts.client.KPIRequestClient;
import com.cts.dto.KPIDTO;
import com.cts.entity.KPI;
import com.cts.mapper.KPIMapper;
import com.cts.repository.KPIRepository;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class KPIService {

    private static final Logger log = LoggerFactory.getLogger(KPIService.class);

    private final KPIRepository kpiRepository;
    private final KPIMapper kpiMapper;

    private final KPIRequestClient   kpiRequestClient;
    private final KPIDeliveryClient  kpiDeliveryClient;
    private final KPIInventoryClient kpiInventoryClient;

    @Autowired
    public KPIService(KPIRepository kpiRepository,
                      KPIMapper kpiMapper,
                      KPIRequestClient kpiRequestClient,
                      KPIDeliveryClient kpiDeliveryClient,
                      KPIInventoryClient kpiInventoryClient) {
        this.kpiRepository       = kpiRepository;
        this.kpiMapper           = kpiMapper;
        this.kpiRequestClient    = kpiRequestClient;
        this.kpiDeliveryClient   = kpiDeliveryClient;
        this.kpiInventoryClient  = kpiInventoryClient;
    }

    
    public List<KPIDTO> getAllKPIs() {
        return kpiRepository.findAll()
                .stream()
                .map(kpiMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<KPIDTO> getKPIsByCategory(String category) {
        return kpiRepository.findByCategory(category)
                .stream()
                .map(kpiMapper::toDTO)
                .collect(Collectors.toList());
    }

    public KPIDTO getKPIById(Long id) {
        KPI kpi = kpiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KPI metric not found with id: " + id));
        return kpiMapper.toDTO(kpi);
    }

    
    @Transactional
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public KPIDTO createKPI(KPIDTO kpiDto) {
        if (kpiRepository.findByName(kpiDto.getName()).isPresent()) {
            throw new RuntimeException("A KPI with name '" + kpiDto.getName() + "' already exists.");
        }
        KPI kpi = kpiMapper.toEntity(kpiDto);
        return kpiMapper.toDTO(kpiRepository.save(kpi));
    }

    @Transactional
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public KPIDTO updateKPI(Long id, KPIDTO kpiDto) {
        KPI existing = kpiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KPI not found with id: " + id));
        kpiMapper.updateEntityFromDto(kpiDto, existing);
        return kpiMapper.toDTO(kpiRepository.save(existing));
    }

    @Recover
    public KPIDTO recoverKPITask(Exception e, KPIDTO kpiDto) {
        log.error("KPI operation failed after retries: {}", e.getMessage());
        return null;
    }

    @Recover
    public KPIDTO recoverKPIUpdate(Exception e, Long id, KPIDTO kpiDto) {
        log.error("KPI update failed for ID {}: {}", id, e.getMessage());
        return null;
    }

    @Transactional
    public void syncRemoteKpis() {
        syncFulfillmentKpi();
        syncCompletionKpi();
        syncUtilizationKpi();
    }

    private void syncFulfillmentKpi() {
        try {
            JsonNode data = kpiRequestClient.getFulfillmentMetrics();
            if (data.has("error")) {
                log.warn("Skipping fulfillment KPI sync: {}", data.get("error").asText());
                return;
            }
            upsertKpi(
                "Request Fulfillment Rate",
                "Percentage of approved department requests",
                "REQUESTS",
                data.path("fulfillmentRate").asText("0") + "%",
                "monthly"
            );
        } catch (Exception e) {
            log.error("Failed to sync fulfillment KPI: {}", e.getMessage());
        }
    }

    private void syncCompletionKpi() {
        try {
            JsonNode data = kpiDeliveryClient.getCompletionStats();
            if (data.has("error")) {
                log.warn("Skipping completion KPI sync: {}", data.get("error").asText());
                return;
            }
            upsertKpi(
                "Delivery Completion Rate",
                "Percentage of deliveries marked CLOSED",
                "DELIVERY",
                data.path("completionRate").asText("0") + "%",
                "monthly"
            );
        } catch (Exception e) {
            log.error("Failed to sync completion KPI: {}", e.getMessage());
        }
    }

    private void syncUtilizationKpi() {
        try {
            JsonNode data = kpiInventoryClient.getStockUtilizationData();
            if (data.has("error")) {
                log.warn("Skipping utilization KPI sync: {}", data.get("error").asText());
                return;
            }
            upsertKpi(
                "Stock Utilization Rate",
                "Ratio of dispatched stock to total inventory",
                "INVENTORY",
                data.path("utilizationRate").asText("0") + "%",
                "monthly"
            );
        } catch (Exception e) {
            log.error("Failed to sync utilization KPI: {}", e.getMessage());
        }
    }

    private void upsertKpi(String name, String definition, String category,
                           String currentValue, String period) {
        KPI kpi = kpiRepository.findByName(name).orElseGet(() -> {
            KPI k = new KPI();
            k.setName(name);
            k.setDefinition(definition);
            k.setCategory(category);
            k.setTarget("95%");
            k.setReportingPeriod(period);
            return k;
        });
        kpi.setCurrentValue(currentValue);
        kpiRepository.save(kpi);
        log.info("KPI '{}' synced → {}", name, currentValue);
    }
}