package com.tde.mescloud.api.rest;

import com.tde.mescloud.model.dto.*;
import com.tde.mescloud.service.KpiService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/kpi")
@AllArgsConstructor
public class KpiController {

    private final KpiService kpiService;

    @PostMapping("/equipment-counts")
    public ResponseEntity<CountingEquipmentKpiDto[]> getCountingEquipmentKpi(@RequestBody KpiFilterDto filter) {
        CountingEquipmentKpiDto[] countingEquipmentKpiDto = kpiService.computeEquipmentKpi(filter);
        return new ResponseEntity<>(countingEquipmentKpiDto, HttpStatus.OK);
    }

    @PostMapping("/{equipmentId}/scheduled-time")
    public ResponseEntity<Long> getEquipmentScheduledTime(@RequestParam long equipmentId, @RequestBody RequestKpiDto filter) {
        Long scheduledTime = kpiService.getTotalScheduledTime(equipmentId, filter);
        return new ResponseEntity<>(scheduledTime, HttpStatus.OK);
    }

    @PostMapping("/{equipmentId}/availability")
    public ResponseEntity<KpiDto> getEquipmentAvailability(@RequestParam long equipmentId, @RequestBody RequestKpiDto filter) {
        KpiDto kpiAvailabilityDto = kpiService.computeAvailability(equipmentId, filter);
        return new ResponseEntity<>(kpiAvailabilityDto, HttpStatus.OK);
    }

    @GetMapping("/{equipmentId}")
    public ResponseEntity<EquipmentKpiAggregatorDto> getEquipmentKpiAggregator(@RequestParam long equipmentId, @RequestBody RequestKpiDto filter) {
        try {
            EquipmentKpiAggregatorDto kpiAggregatorDto = kpiService.getEquipmentKpiAggregator(equipmentId, filter);
            return new ResponseEntity<>(kpiAggregatorDto, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
