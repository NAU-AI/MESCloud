package com.alcegory.mescloud.protocol;

import com.alcegory.mescloud.constant.MqttDTOConstants;
import com.alcegory.mescloud.model.dto.mqqt.PlcMqttDto;
import com.alcegory.mescloud.model.entity.equipment.CountingEquipmentEntity;
import com.alcegory.mescloud.service.alarm.AlarmService;
import com.alcegory.mescloud.service.equipment.CountingEquipmentService;
import com.alcegory.mescloud.service.management.CountingEquipmentManagementService;
import com.alcegory.mescloud.service.record.CounterRecordService;
import com.amazonaws.services.iot.client.AWSIotMessage;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

@Service
@Log
@AllArgsConstructor
public class CounterRecordProcess extends AbstractMesProtocolProcess<PlcMqttDto> {


    private final CounterRecordService counterRecordService;
    private final CountingEquipmentService equipmentService;
    private final CountingEquipmentManagementService countingEquipmentManagementService;
    private final AlarmService alarmService;


    @Override
    public void execute(PlcMqttDto equipmentCounts, AWSIotMessage message) {

        log.info("Executing Counter Record process");
        countingEquipmentManagementService.updateEquipmentStatus(equipmentCounts.getEquipmentCode(), equipmentCounts.getEquipmentStatus());
        alarmService.processAlarms(equipmentCounts);

        if (areInvalidContinuationCounts(equipmentCounts)) {
            log.warning(() -> String.format("Invalid continuation count - Production Order [%s] has no initial records or does not exist",
                    equipmentCounts.getProductionOrderCode()));
        }

        validateProductionOrder(equipmentCounts.getEquipmentCode(), equipmentCounts.getProductionOrderCode());
        setOperationStatus(equipmentCounts);
        counterRecordService.processCounterRecord(equipmentCounts);
    }

    private boolean areInvalidContinuationCounts(PlcMqttDto equipmentCountsMqttDTO) {
        return !counterRecordService.areValidContinuationCounts(equipmentCountsMqttDTO.getProductionOrderCode());
    }

    @Override
    public String getMatchingDTOName() {
        return MqttDTOConstants.COUNTING_RECORD_DTO_NAME;
    }

    private void setOperationStatus(PlcMqttDto equipmentCounts) {
        if (equipmentCounts.getProductionOrderCode().isEmpty()) {
            log.info(() -> String.format("Change status to IDLE for Equipment with code [%s]", equipmentCounts.getEquipmentCode()));
            equipmentService.setOperationStatusByCode(equipmentCounts.getEquipmentCode(), CountingEquipmentEntity.OperationStatus.IDLE);
        } else {
            equipmentService.setOperationStatusByCode(equipmentCounts.getEquipmentCode(), CountingEquipmentEntity.OperationStatus.IN_PROGRESS);
        }
    }

    private void validateProductionOrder(String equipmentCode, String productionOrderCode) {
        counterRecordService.validateProductionOrder(equipmentCode, productionOrderCode);
    }
}
