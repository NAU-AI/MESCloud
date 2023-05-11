package com.tde.mescloud.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EquipmentOutput {

    private long id;
    private CountingEquipment countingEquipment;
    private String code;
    private String alias;
}
