package com.alcegory.mescloud.repository;

import com.alcegory.mescloud.model.entity.ProductionOrderTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductionOrderTemplateRepository extends JpaRepository<ProductionOrderTemplateEntity, Long> {
    
}