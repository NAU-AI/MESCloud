package com.tde.mescloud.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CounterRecordDto {
    private long id;
    private String equipmentAlias;
    private String productionOrderCode;
    private String equipmentOutputAlias;
    private int computedValue;
    private LocalDateTime registeredAt;
    private boolean isValidForProduction;
}
