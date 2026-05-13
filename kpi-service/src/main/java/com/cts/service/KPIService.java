package com.cts.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.dto.KPIDTO;
import com.cts.entity.KPI;
import com.cts.mapper.KPIMapper;
import com.cts.repository.KPIRepository;

@Service
public class KPIService {

    private final KPIRepository kpiRepository;
    private final KPIMapper kpiMapper;

    @Autowired
    public KPIService(KPIRepository kpiRepository, KPIMapper kpiMapper) {
        this.kpiRepository = kpiRepository;
        this.kpiMapper = kpiMapper;
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
                .orElseThrow(() ->
                        new RuntimeException("KPI metric not found with id: " + id));

        return kpiMapper.toDTO(kpi);
    }

    @Transactional
    @Retryable(
        value = { Exception.class }, 
        maxAttempts = 3, 
        backoff = @Backoff(delay = 2000)
    )
    public KPIDTO createKPI(KPIDTO kpiDto) {
        if (kpiRepository.findByName(kpiDto.getName()).isPresent()) {
            throw new RuntimeException(
                "A KPI with name '" + kpiDto.getName() + "' already exists."
            );
        }

        KPI kpi = kpiMapper.toEntity(kpiDto);
        KPI savedKpi = kpiRepository.save(kpi);

        return kpiMapper.toDTO(savedKpi);
    }
    
    @Transactional
    @Retryable(
        value = { Exception.class }, 
        maxAttempts = 3, 
        backoff = @Backoff(delay = 2000)
    )
    public KPIDTO updateKPI(Long id, KPIDTO kpiDto) {
        KPI existingKpi = kpiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KPI not found with id: " + id));

        kpiMapper.updateEntityFromDto(kpiDto, existingKpi);
        KPI updatedKpi = kpiRepository.save(existingKpi);

        return kpiMapper.toDTO(updatedKpi);
    }

    // Recovery method specific to KPI operations
    @Recover
    public KPIDTO recoverKPITask(Exception e, KPIDTO kpiDto) {
        System.err.println("KPI operation failed after retries: " + e.getMessage());
        // Return null or a DTO with an error status to keep the UI from crashing
        return null;
    }

    @Recover
    public KPIDTO recoverKPIUpdate(Exception e, Long id, KPIDTO kpiDto) {
        System.err.println("KPI update failed for ID " + id + ": " + e.getMessage());
        return null;
    }
}
