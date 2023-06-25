package com.tde.mescloud.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CountingEquipmentDto {

    private long id;
    private String code;
    private String alias;
    private int equipmentStatus;
    private boolean hasActiveProductionOrder;
    private int pTimerCommunicationCycle;
    private List<EquipmentOutputDto> outputs;
}