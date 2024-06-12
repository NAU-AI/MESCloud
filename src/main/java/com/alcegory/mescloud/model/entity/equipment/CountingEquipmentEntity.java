package com.alcegory.mescloud.model.entity.equipment;

import com.alcegory.mescloud.model.entity.FeatureEntity;
import com.alcegory.mescloud.model.entity.ImsEntity;
import com.alcegory.mescloud.model.entity.company.SectionEntity;
import com.alcegory.mescloud.model.entity.production.ProductionOrderEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Entity(name = "counting_equipment")
public class CountingEquipmentEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    private String alias;

    @JsonIgnore
    @OneToMany(mappedBy = "equipment", fetch = FetchType.LAZY)
    private List<ProductionOrderEntity> productionOrders;

    @JsonIgnore
    @OneToMany(mappedBy = "countingEquipment", fetch = FetchType.LAZY)
    private List<EquipmentStatusRecordEntity> equipmentStatusRecords;

    @ManyToOne
    @JoinColumn(name = "template_id")
    private TemplateEntity template;


    @ManyToOne
    private SectionEntity section;

    private int equipmentStatus;

    private int pTimerCommunicationCycle;

    @OneToOne(
            fetch = FetchType.EAGER,
            cascade = {CascadeType.PERSIST}
    )
    @JoinColumn(name = "ims_id", referencedColumnName = "id")
    private ImsEntity ims;

    @OneToMany(
            mappedBy = "countingEquipment",
            fetch = FetchType.EAGER,
            cascade = CascadeType.ALL
    )
    private List<EquipmentOutputEntity> outputs;

    private Double theoreticalProduction;

    private Double qualityTarget;
    private Double availabilityTarget;
    private Double performanceTarget;
    private Double overallEquipmentEffectivenessTarget;
    private int unrecognizedAlarmDuration;

    @Enumerated(EnumType.STRING)
    private OperationStatus operationStatus;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "counting_equipment_feature",
            joinColumns = @JoinColumn(name = "counting_equipment_id"),
            inverseJoinColumns = @JoinColumn(name = "feature_id"))
    private List<FeatureEntity> features;

    @Getter
    public enum OperationStatus {
        PENDING("PENDING"),
        IN_PROGRESS("IN_PROGRESS"),
        IDLE("IDLE");

        private final String value;

        OperationStatus(String value) {
            this.value = value;
        }

    }
}