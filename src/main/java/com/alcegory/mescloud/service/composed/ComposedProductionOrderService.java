package com.alcegory.mescloud.service.composed;

import com.alcegory.mescloud.model.dto.pagination.PaginatedComposedDto;
import com.alcegory.mescloud.model.dto.composed.ComposedProductionOrderDto;
import com.alcegory.mescloud.model.dto.composed.ComposedSummaryDto;
import com.alcegory.mescloud.model.dto.production.ProductionOrderDto;
import com.alcegory.mescloud.model.entity.composed.ComposedProductionOrderEntity;
import com.alcegory.mescloud.model.filter.Filter;
import com.alcegory.mescloud.model.request.RequestComposedDto;

import java.util.List;
import java.util.Optional;

public interface ComposedProductionOrderService {

    Optional<ComposedProductionOrderDto> create(RequestComposedDto productionOrderIds);

    Optional<ComposedProductionOrderDto> create(List<Long> productionOrderIds);

    ComposedProductionOrderEntity saveAndUpdate(ComposedProductionOrderEntity composedEntity);

    void delete(ComposedProductionOrderEntity composedEntity);

    Optional<ComposedProductionOrderEntity> findById(Long id);

    List<ComposedProductionOrderDto> getAll();

    default PaginatedComposedDto findAllSummarizedWithHits(long sectionId) {
        return findAllSummarized(sectionId, true);
    }

    default PaginatedComposedDto findAllSummarizedWithoutHits(long sectionId) {
        return findAllSummarized(sectionId, false);
    }

    default PaginatedComposedDto findSummarizedWithHitsFiltered(long sectionId, Filter filter) {
        return findSummarizedFiltered(sectionId, true, filter);
    }

    default PaginatedComposedDto findSummarizedWithoutHitsFiltered(long sectionId, Filter filter) {
        return findSummarizedFiltered(sectionId, false, filter);
    }

    PaginatedComposedDto findSummarizedFiltered(long sectionId, boolean withHits, Filter filter);

    PaginatedComposedDto findAllSummarized(long sectionId, boolean withHits);

    List<ComposedSummaryDto> findAllCompleted(long sectionId);

    PaginatedComposedDto findCompletedFiltered(long sectionId, Filter filter);

    void setProductionOrderApproval(ComposedProductionOrderEntity composed, boolean isApproved);

    void setHitInsertAtInComposed(ComposedProductionOrderEntity composed);

    List<ProductionOrderDto> getProductionOrderSummaryByComposedId(Long composedId);

    void deleteComposed(ComposedProductionOrderEntity composedProductionOrder);
}