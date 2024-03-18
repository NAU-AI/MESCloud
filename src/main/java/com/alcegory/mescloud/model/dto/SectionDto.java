package com.alcegory.mescloud.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SectionDto {

    private Long id;
    private String name;
    private SectionConfigDto sectionConfig;
    //private List<CountingEquipmentSummaryDto> countingEquipments;
}
