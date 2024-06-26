package com.alcegory.mescloud.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Entity(name = "equipment_output_alias")
@Getter
@Setter
public class EquipmentOutputAliasEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String alias;

    @OneToMany(mappedBy = "alias", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EquipmentOutputEntity> equipmentOutputs;
}
