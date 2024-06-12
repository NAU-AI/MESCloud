package com.alcegory.mescloud.service.kpi;

import com.alcegory.mescloud.exception.IncompleteConfigurationException;
import com.alcegory.mescloud.model.converter.CounterRecordConverter;
import com.alcegory.mescloud.model.dto.CounterRecordDto;
import com.alcegory.mescloud.model.dto.equipment.CountingEquipmentDto;
import com.alcegory.mescloud.model.dto.equipment.CountingEquipmentKpiDto;
import com.alcegory.mescloud.model.dto.equipment.EquipmentKpiAggregatorDto;
import com.alcegory.mescloud.model.dto.equipment.EquipmentKpiDto;
import com.alcegory.mescloud.model.dto.kpi.KpiDto;
import com.alcegory.mescloud.model.dto.kpi.TargetValuesDto;
import com.alcegory.mescloud.model.entity.records.CounterRecordSummaryEntity;
import com.alcegory.mescloud.model.filter.FilterDto;
import com.alcegory.mescloud.service.equipment.CountingEquipmentService;
import com.alcegory.mescloud.service.record.CounterRecordService;
import com.alcegory.mescloud.utility.DateUtil;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.alcegory.mescloud.model.filter.Filter.Property.END_DATE;
import static com.alcegory.mescloud.model.filter.Filter.Property.START_DATE;

@Service
@AllArgsConstructor
@Log
public class KpiManagementServiceImpl implements KpiManagementService {

    private final QualityKpiService qualityKpiService;
    private final AvailabilityKpiService availabilityKpiService;
    private final PerformanceKpiService performanceKpiService;
    private final CounterRecordService counterRecordService;
    private final CountingEquipmentService countingEquipmentService;
    private final CounterRecordConverter counterRecordConverter;

    @Override
<<<<<<< HEAD
    public CountingEquipmentKpiDto[] getEquipmentOutputProductionPerDay(FilterDto filter) {
        List<CounterRecordSummaryEntity> equipmentCounts = counterRecordService.getEquipmentOutputProductionPerDay(filter);
=======
    public CountingEquipmentKpiDto[] getEquipmentOutputProductionPerDay(long sectionId, FilterDto filter) {
        List<CounterRecordSummaryEntity> equipmentCounts = counterRecordService.getEquipmentOutputProductionPerDay(sectionId, filter);
>>>>>>> test_environment
        List<CounterRecordDto> counterRecordDto = counterRecordConverter.toDtoList(equipmentCounts);
        return sortPerDay(filter, counterRecordDto);
    }

    @Override
<<<<<<< HEAD
    public CountingEquipmentKpiDto[] computeEquipmentKpi(FilterDto filter) {
        List<CounterRecordDto> equipmentCounts = counterRecordService.filterConclusionRecordsKpi(filter);
=======
    public CountingEquipmentKpiDto[] computeEquipmentKpi(long sectionId, FilterDto filter) {
        List<CounterRecordDto> equipmentCounts = counterRecordService.filterConclusionRecordsKpi(sectionId, filter);
>>>>>>> test_environment
        return sortPerDay(filter, equipmentCounts);
    }

    private CountingEquipmentKpiDto[] sortPerDay(FilterDto filter, List<CounterRecordDto> equipmentCounts) {
        if (equipmentCounts.isEmpty()) {
            return new CountingEquipmentKpiDto[0];
        }

        Map<String, CountingEquipmentKpiDto> equipmentKpiByEquipmentAlias = new LinkedHashMap<>();

        Timestamp startDate = filter.getSearch().getTimestampValue(START_DATE);
        Timestamp endDate = filter.getSearch().getTimestampValue(END_DATE);

        final int spanInDays = DateUtil.spanInDays(startDate, endDate);

        for (CounterRecordDto equipmentCount : equipmentCounts) {
            String equipmentAlias = equipmentCount.getEquipmentAlias();
            CountingEquipmentKpiDto equipmentKpi = equipmentKpiByEquipmentAlias.computeIfAbsent(equipmentAlias,
                    equipmentAliasKey -> new CountingEquipmentKpiDto(equipmentAliasKey, spanInDays));

            final int timeUnitAsIndex = DateUtil.differenceInDays(startDate, equipmentCount.getRegisteredAt());
            equipmentKpi.updateCounts(timeUnitAsIndex, equipmentCount);
        }

        return equipmentKpiByEquipmentAlias.values()
                .toArray(new CountingEquipmentKpiDto[equipmentKpiByEquipmentAlias.size()]);
    }

    @Override
    public EquipmentKpiAggregatorDto computeAllEquipmentKpiAggregator(FilterDto filter)
            throws NoSuchElementException, IncompleteConfigurationException, ArithmeticException {

        TargetValuesDto targetValuesDto = getTargetValues(null);
<<<<<<< HEAD
        return computeEquipmentKpiAggregator(null, filter, targetValuesDto.getAvailabilityTarget(),
                targetValuesDto.getAvailabilityTarget(), targetValuesDto.getPerformanceTarget(),
                targetValuesDto.getOverallEffectivePerformanceTarget(), targetValuesDto.getTheoreticalProduction());
=======
        return computeEquipmentKpiAggregator(null, filter, targetValuesDto);
>>>>>>> test_environment
    }

    @Override
    public EquipmentKpiAggregatorDto computeEquipmentKpiAggregatorById(Long equipmentId, FilterDto filter)
            throws NoSuchElementException, IncompleteConfigurationException, ArithmeticException {

<<<<<<< HEAD
        TargetValuesDto targetValuesDto = getTargetValues(null);
        return computeEquipmentKpiAggregator(equipmentId, filter, targetValuesDto.getAvailabilityTarget(),
                targetValuesDto.getAvailabilityTarget(), targetValuesDto.getPerformanceTarget(),
                targetValuesDto.getOverallEffectivePerformanceTarget(), targetValuesDto.getTheoreticalProduction());
    }

    private EquipmentKpiAggregatorDto computeEquipmentKpiAggregator(Long equipmentId, FilterDto filter,
                                                                    Double qualityTarget, Double availabilityTarget,
                                                                    Double performanceTarget, Double overallEffectivePerformanceTarget,
                                                                    Double theoreticalProduction) {

        KpiDto qualityKpi = qualityKpiService.computeQuality(equipmentId, filter);
        EquipmentKpiDto quality = new EquipmentKpiDto(qualityTarget, qualityKpi);

        KpiDto availabilityKpi = availabilityKpiService.computeAvailability(equipmentId, filter);
        EquipmentKpiDto availability = new EquipmentKpiDto(availabilityTarget, availabilityKpi);

        KpiDto performanceKpi = performanceKpiService.computePerformance(qualityKpi, availabilityKpi, theoreticalProduction);
        EquipmentKpiDto performance = new EquipmentKpiDto(performanceTarget, performanceKpi);

        Double overallEffectivePerformance = computeOverallEffectivePerformance(qualityKpi, availabilityKpi, performanceKpi);
        EquipmentKpiDto overallEquipmentEffectiveness =
                new EquipmentKpiDto(overallEffectivePerformanceTarget, overallEffectivePerformance);
=======
        TargetValuesDto targetValuesDto = getTargetValues(equipmentId);
        return computeEquipmentKpiAggregator(equipmentId, filter, targetValuesDto);
    }

    private EquipmentKpiAggregatorDto computeEquipmentKpiAggregator(Long equipmentId, FilterDto filter, TargetValuesDto targetValues) {

        KpiDto qualityKpi = qualityKpiService.computeQuality(equipmentId, filter);
        EquipmentKpiDto quality = new EquipmentKpiDto(targetValues.getQualityTarget(), qualityKpi);

        KpiDto availabilityKpi = availabilityKpiService.computeAvailability(equipmentId, filter);
        EquipmentKpiDto availability = new EquipmentKpiDto(targetValues.getAvailabilityTarget(), availabilityKpi);

        KpiDto performanceKpi = performanceKpiService.computePerformance(qualityKpi, availabilityKpi, targetValues.getTheoreticalProduction());
        EquipmentKpiDto performance = new EquipmentKpiDto(targetValues.getPerformanceTarget(), performanceKpi);

        Double overallEffectivePerformance = computeOverallEffectivePerformance(qualityKpi, availabilityKpi, performanceKpi);
        EquipmentKpiDto overallEquipmentEffectiveness = new EquipmentKpiDto(targetValues.getOverallEffectivePerformanceTarget(), overallEffectivePerformance);
>>>>>>> test_environment

        return EquipmentKpiAggregatorDto.builder()
                .qualityKpi(quality)
                .availabilityKpi(availability)
                .performanceKpi(performance)
                .overallEquipmentEffectivenessKpi(overallEquipmentEffectiveness)
                .build();
    }

<<<<<<< HEAD
    @Override
    public List<EquipmentKpiAggregatorDto> computeEquipmentKpiAggregatorPerDay(FilterDto filter) {
        return computeEquipmentKpiAggregators(null, filter);
    }

    @Override
    public List<EquipmentKpiAggregatorDto> computeEquipmentKpiAggregatorPerDayById(Long equipmentId, FilterDto filter) {
        return computeEquipmentKpiAggregators(equipmentId, filter);
    }

    private List<EquipmentKpiAggregatorDto> computeEquipmentKpiAggregators(Long equipmentId, FilterDto filter) {
        
=======
    private List<EquipmentKpiAggregatorDto> computeEquipmentKpiAggregators(Long equipmentId, FilterDto filter) {

>>>>>>> test_environment
        Timestamp startDate = filter.getSearch().getTimestampValue(START_DATE);
        Timestamp endDate = filter.getSearch().getTimestampValue(END_DATE);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<EquipmentKpiAggregatorDto> equipmentKpiAggregators = new ArrayList<>();

        LocalDateTime startLocalDateTime = startDate.toLocalDateTime();
        LocalDateTime endLocalDateTime = endDate.toLocalDateTime().plusDays(1).minusNanos(1);

<<<<<<< HEAD
        CountingEquipmentDto countingEquipment = (equipmentId != null)
                ? countingEquipmentService.findById(equipmentId).orElse(null)
                : null;

        TargetValuesDto targetValues = getTargetValues(countingEquipment);
=======
        TargetValuesDto targetValues = getTargetValues(equipmentId);
>>>>>>> test_environment

        for (LocalDate currentDay = startLocalDateTime.toLocalDate();
             !currentDay.isAfter(endLocalDateTime.toLocalDate());
             currentDay = currentDay.plusDays(1)) {

            LocalDateTime startOfDay = currentDay.atStartOfDay();
            LocalDateTime endOfDay = currentDay.plusDays(1).atStartOfDay().minusNanos(1);

            String startDateFilter = startOfDay.format(formatter);
            String endDateTimeFilter = endOfDay.format(formatter);

            filter.getSearch().setSearchValueByName(START_DATE, startDateFilter);
            filter.getSearch().setSearchValueByName(END_DATE, endDateTimeFilter);

            EquipmentKpiAggregatorDto aggregator = computeEquipmentKpiAggregator(
<<<<<<< HEAD
                    equipmentId, filter, targetValues.getQualityTarget(), targetValues.getAvailabilityTarget(),
                    targetValues.getPerformanceTarget(), targetValues.getOverallEffectivePerformanceTarget(),
                    targetValues.getTheoreticalProduction());
=======
                    equipmentId, filter, targetValues);
>>>>>>> test_environment

            equipmentKpiAggregators.add(aggregator);
        }

        return equipmentKpiAggregators;
    }

<<<<<<< HEAD
=======
    @Override
    public List<EquipmentKpiAggregatorDto> computeEquipmentKpiAggregatorPerDay(FilterDto filter) {
        return computeEquipmentKpiAggregators(null, filter);
    }

    @Override
    public List<EquipmentKpiAggregatorDto> computeEquipmentKpiAggregatorPerDayById(Long equipmentId, FilterDto filter) {
        return computeEquipmentKpiAggregators(equipmentId, filter);
    }

>>>>>>> test_environment
    private Double computeOverallEffectivePerformance(KpiDto quality, KpiDto availability, KpiDto performance) {
        if (isValueZeroOrMissing(quality) || isValueZeroOrMissing(availability) || isValueZeroOrMissing(performance)) {
            return null;
        }
        return quality.getValue() * availability.getValue() * performance.getValue();
    }

    private boolean isValueZeroOrMissing(KpiDto kpiDto) {
        return kpiDto == null || kpiDto.getValue() == null || kpiDto.getValue() == 0;
    }

<<<<<<< HEAD
    private TargetValuesDto getTargetValues(CountingEquipmentDto countingEquipment) {
=======
    private TargetValuesDto getTargetValues(Long equipmentId) {
        CountingEquipmentDto countingEquipment = (equipmentId != null)
                ? countingEquipmentService.findById(equipmentId).orElse(null)
                : null;

>>>>>>> test_environment
        if (countingEquipment == null) {
            return new TargetValuesDto(
                    countingEquipmentService.getAverageQualityTargetDividedByTotalCount(),
                    countingEquipmentService.getAverageAvailabilityTargetDividedByTotalCount(),
                    countingEquipmentService.getAveragePerformanceTargetDividedByTotalCount(),
                    countingEquipmentService.getAverageOverallEquipmentEffectivenessTargetDividedByTotalCount(),
                    countingEquipmentService.getAverageTheoreticalProduction()
            );
        } else {
            return new TargetValuesDto(
                    countingEquipment.getQualityTarget(),
                    countingEquipment.getAvailabilityTarget(),
                    countingEquipment.getPerformanceTarget(),
                    countingEquipment.getOverallEquipmentEffectivenessTarget(),
                    countingEquipment.getTheoreticalProduction()
            );
        }
    }
}
