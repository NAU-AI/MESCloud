package com.alcegory.mescloud.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PaginatedComposedDto {
    private List<ComposedSummaryDto> composedProductionOrders;
    private boolean hasNextPage;
}
