package com.alcegory.mescloud.service;

import com.alcegory.mescloud.repository.EquipmentOutputRepository;
import com.alcegory.mescloud.model.converter.GenericConverter;
import com.alcegory.mescloud.model.dto.EquipmentOutputDto;
import com.alcegory.mescloud.model.entity.EquipmentOutputEntity;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
@Log
public class EquipmentOutputServiceImpl implements EquipmentOutputService {

    private final EquipmentOutputRepository repository;
    private final GenericConverter<EquipmentOutputEntity, EquipmentOutputDto> converter;


    @Override
    public Optional<EquipmentOutputDto> findByCode(String equipmentOutputCode) {

        Optional<EquipmentOutputEntity> entity = repository.findByCode(equipmentOutputCode);
        if (entity.isEmpty()) {
            log.warning(() -> String.format("Unable to find an equipment output with the code [%s]", equipmentOutputCode));
            return Optional.empty();
        }

        EquipmentOutputDto equipmentOutput = converter.toDto(entity.get(), EquipmentOutputDto.class);
        return Optional.of(equipmentOutput);
    }
}