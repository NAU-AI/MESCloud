package com.alcegory.mescloud.repository;

import com.alcegory.mescloud.model.filter.CounterRecordFilter;
import com.alcegory.mescloud.model.dto.KpiFilterDto;
import com.alcegory.mescloud.model.entity.CounterRecordConclusionEntity;
import com.alcegory.mescloud.model.entity.CounterRecordEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface CounterRecordRepository extends CrudRepository<CounterRecordEntity, Long> {

    @Query(value = "SELECT * FROM counter_record cr WHERE (cr.production_order_id = :productionOrderId AND cr.equipment_output_id = :equipmentOutputId) ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Optional<CounterRecordEntity> findLastByProductionOrderId(Long productionOrderId, Long equipmentOutputId);

    @Query(value = "SELECT * FROM counter_record cr WHERE (cr.production_order_id = :productionOrderId) LIMIT 1", nativeQuery = true)
    Optional<CounterRecordEntity> findLastByProductionOrderId(Long productionOrderId);

    List<CounterRecordConclusionEntity> findLastPerProductionOrder(CounterRecordFilter filterDto);

    List<CounterRecordConclusionEntity> findLastPerProductionOrder(KpiFilterDto filterDto);

    //    @EntityGraph(attributePaths = { "equipmentOutput", "equipmentOutput.countingEquipment", "productionOrder" })
    List<CounterRecordEntity> getFilteredAndPaginated(CounterRecordFilter filterDto);

    Integer sumValidCounterIncrement(Long countingEquipmentId, Timestamp startDateFilter, Timestamp endDateFilter);

    Integer sumValidCounterIncrementForApprovedPO(Long countingEquipmentId, Timestamp startDateFilter, Timestamp endDateFilter);

    Integer sumCounterIncrement(Long countingEquipmentId, Timestamp startDateFilter, Timestamp endDateFilter);

    @Query(value = "SELECT * FROM counter_record WHERE production_order_id = "
            + "(SELECT id FROM production_order WHERE code = :productionOrderCode) "
            + "ORDER BY id DESC LIMIT 1", nativeQuery = true)
    CounterRecordEntity getLastCounterRecord(@Param("productionOrderCode") String productionOrderCode);

    @Query(value = "SELECT cr.computed_active_time FROM counter_record cr " +
            "WHERE cr.production_order_id = :productionOrderId " +
            "AND cr.registered_at <= :endDate " +
            "ORDER BY cr.registered_at DESC " +
            "LIMIT 1", nativeQuery = true)
    Long getComputedActiveTimeByProductionOrderId(
            @Param("productionOrderId") Long productionOrderId,
            @Param("endDate") Timestamp endDate);
}