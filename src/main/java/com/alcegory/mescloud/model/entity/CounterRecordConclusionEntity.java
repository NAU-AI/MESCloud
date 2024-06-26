package com.alcegory.mescloud.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "counter_record_production_conclusion")
@Getter
@Setter
public class CounterRecordConclusionEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_output_id")
    private EquipmentOutputEntity equipmentOutput;
    private String equipmentOutputAlias;
    private int realValue;
    private int computedValue;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_order_id")
    private ProductionOrderEntity productionOrder;
    @Temporal(TemporalType.TIMESTAMP)
    private Date registeredAt;
    private Boolean isValidForProduction;
}
