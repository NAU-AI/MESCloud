package com.alcegory.mescloud.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SectionConfigDto {

    private Long id;
    private String label;
    private int order;
    @JsonIgnore
    private List<FeatureDto> featureList;
}
