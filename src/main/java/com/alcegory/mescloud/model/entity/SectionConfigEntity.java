package com.alcegory.mescloud.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Entity(name = "section_config")
public class SectionConfigEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "section_id")
    private SectionEntity section;

    private String label;

    private int order;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "section_config_feature",
            joinColumns = @JoinColumn(name = "section_config_id"),
            inverseJoinColumns = @JoinColumn(name = "feature_id"))
    private List<FeatureEntity> featureList;
}


