package com.alcegory.mescloud.api.rest;

import com.alcegory.mescloud.model.entity.ProductionOrderTemplateEntity;
import com.alcegory.mescloud.service.TemplateService;
import com.azure.core.annotation.Get;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/template")
@AllArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping("/{id}")
    public ResponseEntity<ProductionOrderTemplateEntity> getTemplate(@PathVariable Long id) {
        try {
            ProductionOrderTemplateEntity template = templateService.getTemplateWithFields(id);
            if (template != null) {
                return ResponseEntity.ok(template);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}