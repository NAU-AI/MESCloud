package com.alcegory.mescloud.service.production;

import com.alcegory.mescloud.model.entity.equipment.TemplateEntity;
import com.alcegory.mescloud.repository.production.ProductionOrderTemplateRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class TemplateServiceImpl implements TemplateService {

    private final ProductionOrderTemplateRepository templateRepository;

    @Override
    public TemplateEntity getTemplateWithFields() {
        Optional<TemplateEntity> optionalTemplate = templateRepository.findById(1L);

        return optionalTemplate.orElseGet(TemplateEntity::new);
    }

}