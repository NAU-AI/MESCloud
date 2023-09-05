package com.tde.mescloud.service;

import com.tde.mescloud.model.converter.BatchConverter;
import com.tde.mescloud.model.dto.BatchDto;
import com.tde.mescloud.model.dto.RequestBatchDto;
import com.tde.mescloud.model.entity.BatchEntity;
import com.tde.mescloud.model.entity.ComposedProductionOrderEntity;
import com.tde.mescloud.repository.BatchRepository;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Log
public class BatchServiceImpl implements BatchService {

    private final BatchRepository repository;
    private final BatchConverter converter;

    private final ComposedProductionOrderService composedService;

    @Override
    public BatchDto create(RequestBatchDto requestBatch) {
        BatchEntity batch = createBatch(requestBatch);
        BatchEntity savedBatch = saveAndUpdate(batch);
        return converter.toDto(savedBatch);
    }

    private BatchEntity createBatch(RequestBatchDto requestBatch) {
        BatchEntity batch = converter.toEntity(requestBatch.getBatch());
        batch.setComposed(getComposedById(requestBatch.getComposedId()));
        return batch;
    }

    private ComposedProductionOrderEntity getComposedById(Long composedId) {
        Optional<ComposedProductionOrderEntity> composedOpt = composedService.findById(composedId);
        if (composedOpt.isEmpty()) {
            throw new IllegalStateException("Composed production order is null or empty");
        }
        return composedOpt.get();
    }

    @Override
    public BatchEntity saveAndUpdate(BatchEntity batch) {
        return repository.save(batch);
    }

    @Override
    public void delete(BatchEntity batch) {
        repository.delete(batch);
    }

    @Override
    public Optional<BatchEntity> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<BatchDto> getAll() {
        return converter.toDto(repository.findAll());
    }
}