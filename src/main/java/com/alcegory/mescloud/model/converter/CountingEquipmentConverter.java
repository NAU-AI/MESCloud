package com.alcegory.mescloud.model.converter;

import com.alcegory.mescloud.model.dto.CountingEquipmentDto;
import com.alcegory.mescloud.model.dto.CountingEquipmentSummaryDto;
import com.alcegory.mescloud.model.entity.CountingEquipmentEntity;
import com.alcegory.mescloud.model.request.RequestConfigurationDto;

import java.util.List;

public interface CountingEquipmentConverter {

    CountingEquipmentDto convertToDto(CountingEquipmentEntity entity);

    CountingEquipmentEntity convertToEntity(CountingEquipmentDto dto);

    CountingEquipmentEntity convertToEntity(RequestConfigurationDto dto);

    List<CountingEquipmentDto> convertToDto(List<CountingEquipmentEntity> countingEquipments);

    List<CountingEquipmentSummaryDto> convertToSummaryDtoList(List<CountingEquipmentEntity> countingEquipments);
}
