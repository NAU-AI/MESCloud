package com.alcegory.mescloud.service.alarm;

import com.alcegory.mescloud.constant.AlarmStatus;
import com.alcegory.mescloud.exception.AlarmConfigurationNotFoundException;
import com.alcegory.mescloud.exception.AlarmNotFoundException;
import com.alcegory.mescloud.exception.EquipmentNotFoundException;
import com.alcegory.mescloud.exception.IllegalAlarmStatusException;
import com.alcegory.mescloud.model.dto.alarm.AlarmCountsDto;
import com.alcegory.mescloud.model.dto.alarm.AlarmDto;
import com.alcegory.mescloud.model.dto.pagination.PaginatedAlarmDto;
import com.alcegory.mescloud.model.dto.mqqt.PlcMqttDto;
import com.alcegory.mescloud.model.filter.Filter;
import com.alcegory.mescloud.model.request.RequestAlarmRecognitionDto;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface AlarmService {

    PaginatedAlarmDto findByFilter(long sectionId, Filter filter);

    List<AlarmDto> findByEquipmentIdAndStatus(Long equipmentId, AlarmStatus status);

    AlarmDto recognizeAlarm(Long alarmId, RequestAlarmRecognitionDto alarmRecognition, Authentication authentication)
            throws AlarmNotFoundException, IllegalAlarmStatusException;

    AlarmCountsDto getAlarmCounts(Filter filter);

    void processAlarms(PlcMqttDto plcMqttDto) throws AlarmConfigurationNotFoundException, EquipmentNotFoundException;
}
