package com.tde.mescloud.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity(name = "counting_equipment")
@Getter
@Setter
public class CountingEquipmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private String alias;
    @ManyToOne
    private SectionEntity section;
    private int equipmentStatus;
    private int pTimerCommunicationCycle;
    @OneToMany(mappedBy = "countingEquipment")
    List<EquipmentOutputEntity> outputs;

}
