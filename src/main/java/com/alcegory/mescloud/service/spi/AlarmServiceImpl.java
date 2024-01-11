package com.alcegory.mescloud.service.spi;

import com.alcegory.mescloud.constant.AlarmStatus;
import com.alcegory.mescloud.exception.AlarmConfigurationNotFoundException;
import com.alcegory.mescloud.exception.AlarmNotFoundException;
import com.alcegory.mescloud.exception.EquipmentNotFoundException;
import com.alcegory.mescloud.exception.IllegalAlarmStatusException;
import com.alcegory.mescloud.model.converter.GenericConverter;
import com.alcegory.mescloud.model.dto.*;
import com.alcegory.mescloud.model.entity.*;
import com.alcegory.mescloud.model.filter.AlarmFilter;
import com.alcegory.mescloud.repository.AlarmRepository;
import com.alcegory.mescloud.service.AlarmConfigurationService;
import com.alcegory.mescloud.service.AlarmService;
import com.alcegory.mescloud.service.CountingEquipmentService;
import com.alcegory.mescloud.service.ProductionOrderService;
import com.alcegory.mescloud.utility.BinaryUtil;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Log
public class AlarmServiceImpl implements AlarmService {

    private static final int PLC_BITS_PER_WORD = 16;
    public static final int ZERO_BASED_OFFSET = 1;

    private GenericConverter<AlarmEntity, AlarmDto> converter;
    private AlarmRepository repository;
    private AlarmConfigurationService alarmCodeService;
    private ProductionOrderService productionOrderService;
    private GenericConverter<ProductionOrderEntity, ProductionOrderDto> productionOrderConverter;
    private CountingEquipmentService countingEquipmentService;
    private GenericConverter<CountingEquipmentEntity, CountingEquipmentDto> countingEquipmentConverter;


    @Override
    public List<AlarmDto> findByFilter(AlarmFilter filter) {
        List<AlarmEntity> alarms = repository.findByFilter(filter);
        return converter.toDto(alarms, AlarmDto.class);
    }

    @Override
    public List<AlarmDto> findByEquipmentIdAndStatus(Long equipmentId, AlarmStatus status) {
        List<AlarmEntity> activeAlarms = repository.findByEquipmentIdAndStatus(equipmentId, status);
        return converter.toDto(activeAlarms, AlarmDto.class);
    }

    @Override
    public AlarmDto recognizeAlarm(Long alarmId, RequestAlarmRecognitionDto alarmRecognition, Authentication authentication)
            throws AlarmNotFoundException, IllegalAlarmStatusException {

        Optional<AlarmEntity> alarmOpt = repository.findById(alarmId);
        if (alarmOpt.isEmpty()) {
            String msg = String.format("Unable to recognize alarm: no alarm found with id [%s]", alarmId);
            log.warning(msg);
            throw new AlarmNotFoundException(msg);
        }

        AlarmEntity alarmToUpdate = alarmOpt.get();
        if (!AlarmStatus.INACTIVE.equals(alarmToUpdate.getStatus())) {
            String alarmStatusName = alarmToUpdate.getStatus() != null ? alarmToUpdate.getStatus().name() : null;
            String msg = String.format("Unable to recognize alarm record: current alarm record status is [%s]", alarmStatusName);
            log.warning(msg);
            throw new IllegalAlarmStatusException(msg);
        }

        UserEntity user = (UserEntity) authentication.getPrincipal();
        return doRecognizeAlarm(alarmToUpdate, user, alarmRecognition.getComment());
    }

    private AlarmDto doRecognizeAlarm(AlarmEntity alarmToUpdate, UserEntity user, @Nullable String comment) {

        alarmToUpdate.setRecognizedBy(user);
        alarmToUpdate.setRecognizedAt(new Date());
        alarmToUpdate.setComment(comment);
        alarmToUpdate.setStatus(AlarmStatus.RECOGNIZED);

        AlarmEntity updatedAlarmRecord = repository.save(alarmToUpdate);
        return converter.toDto(updatedAlarmRecord, AlarmDto.class);
    }

    @Override
    public AlarmCounts getAlarmCounts(AlarmFilter filter) {
        return repository.getAlarmCounts(filter);
    }

    @Override
    public void processAlarms(PlcMqttDto plcMqttDto) throws EquipmentNotFoundException, AlarmConfigurationNotFoundException {

        Optional<CountingEquipmentDto> countingEquipmentOpt = countingEquipmentService.findByCode(plcMqttDto.getEquipmentCode());
        if (countingEquipmentOpt.isEmpty()) {
            String message = String.format("Unable to find Counting Equipment with code [%s]", plcMqttDto.getEquipmentCode());
            log.warning(message);
            throw new EquipmentNotFoundException(message);
        }

        CountingEquipmentDto countingEquipment = countingEquipmentOpt.get();
        countingEquipment.setProductionOrderCode(plcMqttDto.getProductionOrderCode());

        List<AlarmEntity> alarmsToUpsert = processAlarmWords(countingEquipment, plcMqttDto.getAlarms());
        repository.saveAll(alarmsToUpsert);
    }

    private List<AlarmEntity> processAlarmWords(CountingEquipmentDto countingEquipment, int[] plcAlarms) {

        Map<Long, AlarmEntity> activeAlarmByConfigId = findActiveAlarmsMap(countingEquipment.getId());
        List<AlarmEntity> alarmsToUpsert = new ArrayList<>();

        boolean[][] alarmWords = BinaryUtil.toBinaryUnsigned(plcAlarms, PLC_BITS_PER_WORD);
        for (int wordIndex = 0; wordIndex < alarmWords.length; wordIndex++) {
            for (int bitIndex = 0; bitIndex < PLC_BITS_PER_WORD; bitIndex++) {
                if (alarmWords[wordIndex][bitIndex]) {
                    processAlarmBit(activeAlarmByConfigId, alarmsToUpsert, countingEquipment, wordIndex, bitIndex);
                }
            }
        }

        deactivateAlarms(activeAlarmByConfigId, alarmsToUpsert);
        return alarmsToUpsert;
    }

    private void processAlarmBit(Map<Long, AlarmEntity> activeAlarmByConfigId, List<AlarmEntity> alarmsToUpsert, CountingEquipmentDto equipment, int wordIndex, int bitIndex)
            throws AlarmConfigurationNotFoundException {

        int binaryBitIndex = BinaryUtil.flipIndex(PLC_BITS_PER_WORD - ZERO_BASED_OFFSET, bitIndex);
        AlarmConfigurationEntity alarmConfig = findAlarmConfiguration(wordIndex, binaryBitIndex);
        AlarmEntity activeAlarm = activeAlarmByConfigId.get(alarmConfig.getId());

        if (activeAlarm == null) {
            AlarmEntity newActiveAlarm = createActiveAlarm(alarmConfig, equipment);
            alarmsToUpsert.add(newActiveAlarm);
        } else {
            activeAlarmByConfigId.remove(activeAlarm.getAlarmConfiguration().getId());
            log.info(() -> String.format("Alarm with id [%s] and configuration id [%s] is still active", activeAlarm.getId(), activeAlarm.getAlarmConfiguration().getId()));
        }
    }

    private AlarmEntity createActiveAlarm(AlarmConfigurationEntity alarmConfig, CountingEquipmentDto equipment) {
        AlarmEntity newActiveAlarm = createAlarmEntity(alarmConfig, equipment);
        setProductionOrder(newActiveAlarm, equipment.getProductionOrderCode());
        return newActiveAlarm;
    }

    private void deactivateAlarms(Map<Long, AlarmEntity> alarmByConfigId, List<AlarmEntity> alarmsToUpsert) {
        alarmByConfigId.values().forEach(inactiveAlarm -> {
            inactiveAlarm.setStatus(AlarmStatus.INACTIVE);
            inactiveAlarm.setCompletedAt(new Date());
            alarmsToUpsert.add(inactiveAlarm);
        });
    }

    private AlarmConfigurationEntity findAlarmConfiguration(int wordIndex, int bitIndex) throws AlarmConfigurationNotFoundException {
        Optional<AlarmConfigurationEntity> alarmConfigurationOpt = alarmCodeService.findByWordAndBitIndexes(wordIndex, bitIndex);
        if (alarmConfigurationOpt.isEmpty()) {
            String message = String.format("Unable to find an Alarm Configuration for word index [%s] and bit index [%s]", wordIndex, bitIndex);
            log.warning(message);
            throw new AlarmConfigurationNotFoundException(message);
        }
        return alarmConfigurationOpt.get();
    }

    private Map<Long, AlarmEntity> findActiveAlarmsMap(Long equipmentId) {
        List<AlarmEntity> activeAlarms = repository.findByEquipmentIdAndStatus(equipmentId, AlarmStatus.ACTIVE);
        return activeAlarms.stream()
                .collect(Collectors.toMap(alarm -> alarm.getAlarmConfiguration().getId(), alarm -> alarm));
    }

    private AlarmEntity createAlarmEntity(AlarmConfigurationEntity alarmConfiguration, CountingEquipmentDto countingEquipmentDto) {

        CountingEquipmentEntity countingEquipment = countingEquipmentConverter.toEntity(countingEquipmentDto, CountingEquipmentEntity.class);

        AlarmEntity alarmEntity = new AlarmEntity();
        alarmEntity.setAlarmConfiguration(alarmConfiguration);
        alarmEntity.setEquipment(countingEquipment);
        alarmEntity.setCreatedAt(new Date());
        alarmEntity.setStatus(AlarmStatus.ACTIVE);
        return alarmEntity;
    }

    private void setProductionOrder(AlarmEntity alarm, String productionOrderCode) {

        Optional<ProductionOrderDto> productionOrderOpt = productionOrderService.findDtoByCode(productionOrderCode);
        if (productionOrderOpt.isPresent()) {
            ProductionOrderEntity productionOrder = productionOrderConverter.toEntity(productionOrderOpt.get(), ProductionOrderEntity.class);
            alarm.setProductionOrder(productionOrder);
        } else {
            log.warning(() -> String.format("Unable to find Production Order with code [%s]", productionOrderCode));
        }
    }
}
