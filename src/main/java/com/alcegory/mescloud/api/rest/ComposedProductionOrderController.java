package com.alcegory.mescloud.api.rest;

import com.alcegory.mescloud.model.dto.*;
import com.alcegory.mescloud.model.entity.ProductionOrderSummaryEntity;
import com.alcegory.mescloud.service.ComposedProductionOrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/composed-production-order")
@AllArgsConstructor
public class ComposedProductionOrderController {

    private final ComposedProductionOrderService composedService;

    @PostMapping
    public ResponseEntity<ComposedProductionOrderDto> create(@RequestBody RequestComposedDto productionOrderIds) {
        Optional<ComposedProductionOrderDto> composedProductionOpt = composedService.create(productionOrderIds);
        if (composedProductionOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(composedProductionOpt.get(), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<ComposedProductionOrderDto>> findAll() {
        List<ComposedProductionOrderDto> composedDtos = composedService.getAll();
        return new ResponseEntity<>(composedDtos, HttpStatus.OK);
    }

    @GetMapping("/insert-hits")
    public ResponseEntity<List<ComposedSummaryDto>> findAllToInsertHits() {
        List<ComposedSummaryDto> composedWithoutHits = composedService.findSummarizedWithoutHits();
        return new ResponseEntity<>(composedWithoutHits, HttpStatus.OK);
    }

    @GetMapping("/approval")
    public ResponseEntity<List<ComposedSummaryDto>> findAllForApproval() {
        List<ComposedSummaryDto> composedWithoutHits = composedService.findSummarizedWithHits();
        return new ResponseEntity<>(composedWithoutHits, HttpStatus.OK);
    }

    @GetMapping("/completed")
    public ResponseEntity<List<ComposedSummaryDto>> findCompleted() {
        List<ComposedSummaryDto> composedCompleted = composedService.findCompleted();
        return new ResponseEntity<>(composedCompleted, HttpStatus.OK);
    }

    @PostMapping("/production-orders")
    public ResponseEntity<List<ProductionOrderSummaryDto>> getProductionOrderSummaryByComposedId(@RequestBody RequestById request) {
        if (request == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            List<ProductionOrderSummaryDto> productionOrderSummary = composedService.getProductionOrderSummaryByComposedId(request.getId());
            return new ResponseEntity<>(productionOrderSummary, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}