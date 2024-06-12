package com.alcegory.mescloud.service.composed;

import com.alcegory.mescloud.model.converter.GenericConverter;
import com.alcegory.mescloud.model.converter.ProductionOrderConverter;
import com.alcegory.mescloud.model.dto.composed.ComposedProductionOrderDto;
<<<<<<< HEAD
import com.alcegory.mescloud.model.dto.production.ProductionOrderDto;
import com.alcegory.mescloud.model.dto.composed.SampleDto;
import com.alcegory.mescloud.model.entity.composed.ComposedProductionOrderEntity;
import com.alcegory.mescloud.model.entity.production.ProductionOrderEntity;
import com.alcegory.mescloud.model.entity.composed.SampleEntity;
=======
import com.alcegory.mescloud.model.dto.composed.SampleDto;
import com.alcegory.mescloud.model.dto.production.ProductionOrderDto;
import com.alcegory.mescloud.model.entity.composed.ComposedProductionOrderEntity;
import com.alcegory.mescloud.model.entity.composed.SampleEntity;
import com.alcegory.mescloud.model.entity.production.ProductionOrderEntity;
>>>>>>> test_environment
import com.alcegory.mescloud.model.request.RequestById;
import com.alcegory.mescloud.model.request.RequestSampleDto;
import com.alcegory.mescloud.repository.composed.SampleRepository;
import com.alcegory.mescloud.security.service.UserRoleService;
import com.alcegory.mescloud.service.production.ProductionOrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.alcegory.mescloud.security.model.SectionAuthority.ADMIN_DELETE;
import static com.alcegory.mescloud.security.model.SectionAuthority.OPERATOR_CREATE;

@Service
@AllArgsConstructor
@Log
public class SampleServiceImpl implements SampleService {

    private final SampleRepository repository;
    private final GenericConverter<SampleEntity, SampleDto> converter;
    private final GenericConverter<ComposedProductionOrderEntity, ComposedProductionOrderDto> composedConverter;
    private final ProductionOrderConverter productionOrderConverter;

    private final ComposedProductionOrderService composedService;
    private final ProductionOrderService productionOrderService;
    private final UserRoleService userRoleService;

    @Override
    public SampleDto create(long sectionId, RequestSampleDto requestSampleDto, Authentication authentication) {
        userRoleService.checkSectionAuthority(authentication, sectionId, OPERATOR_CREATE);
        ComposedProductionOrderEntity composedEntity = createComposed(requestSampleDto);
        return createSample(requestSampleDto, composedEntity);
    }

    private SampleDto createSample(RequestSampleDto requestSampleDto, ComposedProductionOrderEntity composedEntity) {
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setAmount(requestSampleDto.getAmount());
        sampleEntity.setComposedProductionOrder(composedEntity);
        sampleEntity.setCreatedAt(new Date());

        saveAndUpdate(sampleEntity);
        SampleDto sampleDto = converter.toDto(sampleEntity, SampleDto.class);
        sampleDto.setComposedCode(composedEntity.getCode());
        return sampleDto;
    }

    private ComposedProductionOrderEntity createComposed(RequestSampleDto requestSampleDto) {
        Optional<ComposedProductionOrderDto> composedDto = composedService.create(requestSampleDto.getProductionOrderIds());
        if (composedDto.isEmpty()) {
            throw new IllegalStateException("Composed Production Order creation error");
        }
        return composedConverter.toEntity(composedDto.get(), ComposedProductionOrderEntity.class);
    }

    public SampleEntity saveAndUpdate(SampleEntity sample) {
        return repository.save(sample);
    }

    @Override
    public void delete(SampleEntity sampleEntity) {
        repository.delete(sampleEntity);
    }

    @Override
    public Optional<SampleEntity> findById(Long id) {
        return repository.findById(id);
    }


    @Override
    public List<SampleDto> getAll() {
        return converter.toDto(repository.findAll(), SampleDto.class);
    }

    @Override
    public List<ProductionOrderDto> removeProductionOrderFromComposed(long sectionId, RequestById request, Authentication authentication) {
        userRoleService.checkSectionAuthority(authentication, sectionId, ADMIN_DELETE);

        Optional<ProductionOrderEntity> productionOrderOpt = productionOrderService.findById(request.getId());

        if (productionOrderOpt.isEmpty()) {
            throw new EntityNotFoundException("Production Order not found");
        }

        ProductionOrderEntity productionOrder = productionOrderOpt.get();
        ComposedProductionOrderEntity composedProductionOrder = productionOrder.getComposedProductionOrder();

        productionOrder.setComposedProductionOrder(null);
        productionOrderService.saveAndUpdate(productionOrder);

        List<ProductionOrderEntity> productionOrders = productionOrderService.findByComposedProductionOrderId(composedProductionOrder.getId());
        if (productionOrders.isEmpty()) {
            SampleEntity sampleEntity = repository.findByComposedProductionOrderId(composedProductionOrder.getId());

            if (sampleEntity != null) {
                repository.delete(sampleEntity);
            }

            composedService.delete(composedProductionOrder);
            return Collections.emptyList();
        }

        return productionOrderConverter.toDto(productionOrders);
    }

    @Override
    public SampleEntity findByComposedProductionOrderId(Long composedProductionOrderId) {
        return repository.findByComposedProductionOrderId(composedProductionOrderId);
    }
}
