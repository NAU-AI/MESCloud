package com.alcegory.mescloud.service;

import com.alcegory.mescloud.model.dto.*;
import com.alcegory.mescloud.model.entity.ComposedProductionOrderEntity;

import java.util.List;
import java.util.Optional;

public interface ComposedProductionOrderService {

    Optional<ComposedProductionOrderDto> create(RequestComposedDto productionOrderIds);

    Optional<ComposedProductionOrderDto> create(List<Long> productionOrderIds);

    ComposedProductionOrderEntity saveAndUpdate(ComposedProductionOrderEntity composedEntity);

    void delete(ComposedProductionOrderEntity composedEntity);

    Optional<ComposedProductionOrderEntity> findById(Long id);

    List<ComposedProductionOrderDto> getAll();

    default List<ComposedSummaryDto> findAllSummarizedWithHits() {
        return findAllSummarized(true);
    }

    default List<ComposedSummaryDto> findAllSummarizedWithoutHits() {
        return findAllSummarized(false);
    }

    default List<ComposedSummaryDto> findSummarizedWithHitsFiltered(KpiFilterDto filter) {
        return findSummarizedFiltered(true, filter);
    }

    default List<ComposedSummaryDto> findSummarizedWithoutHitsFiltered(KpiFilterDto filter) {
        return findSummarizedFiltered(false, filter);
    }

    List<ComposedSummaryDto> findSummarizedFiltered(boolean withHits, KpiFilterDto filter);

    List<ComposedSummaryDto> findAllSummarized(boolean withHits);

    List<ComposedSummaryDto> findAllCompleted();

    void setProductionOrderApproval(ComposedProductionOrderEntity composed, boolean isApproved);

    void setHitInsertAtInComposed(ComposedProductionOrderEntity composed);

    List<ProductionOrderSummaryDto> getProductionOrderSummaryByComposedId(Long composedId);
}
