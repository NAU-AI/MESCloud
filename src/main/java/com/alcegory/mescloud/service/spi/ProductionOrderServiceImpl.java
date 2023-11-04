package com.alcegory.mescloud.service.spi;

import com.alcegory.mescloud.api.mqtt.MqttClient;
import com.alcegory.mescloud.constant.MqttDTOConstants;
import com.alcegory.mescloud.exception.MesMqttException;
import com.alcegory.mescloud.model.converter.GenericConverter;
import com.alcegory.mescloud.model.converter.ProductionOrderConverter;
import com.alcegory.mescloud.model.dto.CountingEquipmentDto;
import com.alcegory.mescloud.model.dto.ProductionOrderDto;
import com.alcegory.mescloud.model.dto.ProductionOrderMqttDto;
import com.alcegory.mescloud.model.dto.ProductionOrderSummaryDto;
import com.alcegory.mescloud.model.entity.CountingEquipmentEntity;
import com.alcegory.mescloud.model.entity.ProductionOrderEntity;
import com.alcegory.mescloud.model.entity.ProductionOrderSummaryEntity;
import com.alcegory.mescloud.protocol.MesMqttSettings;
import com.alcegory.mescloud.repository.CountingEquipmentRepository;
import com.alcegory.mescloud.repository.ProductionOrderRepository;
import com.alcegory.mescloud.service.CountingEquipmentService;
import com.alcegory.mescloud.service.ProductionOrderService;
import com.alcegory.mescloud.utility.DateUtil;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Log
public class ProductionOrderServiceImpl implements ProductionOrderService {

    private static final String OBO_SECTION_PREFIX = "OBO";
    private static final String CODE_PREFIX = "PO";
    private static final String FIVE_DIGIT_NUMBER_FORMAT = "%05d";
    private static final int FIRST_CODE_VALUE = 1;

    private final ProductionOrderRepository repository;
    private final ProductionOrderConverter converter;
    private final GenericConverter<ProductionOrderSummaryEntity, ProductionOrderSummaryDto> summaryConverter;
    private final GenericConverter<CountingEquipmentEntity, CountingEquipmentDto> equipmentConverter;
    private final CountingEquipmentService countingEquipmentService;
    private final CountingEquipmentRepository countingEquipmentRepository;
    private final MqttClient mqttClient;
    private final MesMqttSettings mqttSettings;

    @Override
    public Optional<ProductionOrderDto> findByCode(String code) {
        Optional<ProductionOrderEntity> persistedProductionOrderOpt = repository.findByCode(code);
        if (persistedProductionOrderOpt.isEmpty()) {
            return Optional.empty();
        }

        ProductionOrderDto productionOrder = converter.toDto(persistedProductionOrderOpt.get());
        return Optional.of(productionOrder);
    }

    @Override
    public boolean hasActiveProductionOrder(long countingEquipmentId) {
        Optional<ProductionOrderEntity> productionOrderEntityOpt = repository.findActive(countingEquipmentId);
        return productionOrderEntityOpt.isPresent();
    }

    @Override
    public Optional<ProductionOrderDto> complete(long equipmentId) {
        log.info(() -> String.format("Complete process Production Order started for equipmentId [%s]:", equipmentId));

        Optional<CountingEquipmentEntity> countingEquipmentOpt = countingEquipmentRepository.findById(equipmentId);
        if (countingEquipmentOpt.isEmpty()) {
            log.warning(() -> String.format("Unable to find Equipment with id [%s]", equipmentId));
            return Optional.empty();
        }

        CountingEquipmentDto countingEquipmentDto = setOperationStatus(countingEquipmentOpt.get(), CountingEquipmentEntity.OperationStatus.PENDING);
        log.info(() -> String.format("Change status to PENDING for Equipment with code [%s]", countingEquipmentDto.getCode()));

        Optional<ProductionOrderEntity> productionOrderEntityOpt = repository.findActive(equipmentId);
        if (productionOrderEntityOpt.isEmpty()) {
            log.warning(() -> String.format("No active Production Order found for Equipment with id [%s]", equipmentId));
            return Optional.empty();
        }

        publishProductionOrderCompletion(countingEquipmentOpt.get(), productionOrderEntityOpt.get());
        log.info(() -> String.format("Production Order Conclusion already publish for equipmentId [%s]:", equipmentId));

        return setCompleteDate(productionOrderEntityOpt.get());
    }

    public void publishProductionOrderCompletion(CountingEquipmentEntity countingEquipment, ProductionOrderEntity productionOrder) {
        try {
            publishProductionOrderCompletionToPLC(countingEquipment, productionOrder);
        } catch (MesMqttException e) {
            log.severe(() -> String.format("Unable to publish Order Completion to PLC for equipment [%s]",
                    countingEquipment.getCode()));
        }
    }

    private void publishProductionOrderCompletionToPLC(CountingEquipmentEntity countingEquipment, ProductionOrderEntity productionOrder)
            throws MesMqttException {
        ProductionOrderMqttDto productionOrderMqttDto = new ProductionOrderMqttDto();
        productionOrderMqttDto.setJsonType(MqttDTOConstants.PRODUCTION_ORDER_CONCLUSION_DTO_NAME);
        productionOrderMqttDto.setEquipmentEnabled(false);
        productionOrderMqttDto.setProductionOrderCode(productionOrder.getCode());
        productionOrderMqttDto.setTargetAmount(0);
        productionOrderMqttDto.setEquipmentCode(countingEquipment.getCode());
        mqttClient.publish(mqttSettings.getProtCountPlcTopic(), productionOrderMqttDto);
    }

    private Optional<ProductionOrderDto> setCompleteDate(ProductionOrderEntity productionOrderEntity) {
        productionOrderEntity.setCompletedAt(new Date());
        repository.save(productionOrderEntity);
        ProductionOrderDto productionOrderDto = converter.toDto(productionOrderEntity);

        return Optional.of(productionOrderDto);
    }

    @Override
    public Optional<ProductionOrderDto> create(ProductionOrderDto productionOrder) {

        Optional<CountingEquipmentEntity> countingEquipmentEntityOpt =
                countingEquipmentRepository.findById(productionOrder.getEquipmentId());
        if (countingEquipmentEntityOpt.isEmpty()) {
            log.warning(() -> String.format("Unable to create Production Order - no Equipment found with id [%s]",
                    productionOrder.getEquipmentId()));
            return Optional.empty();
        }

        ProductionOrderEntity productionOrderEntity = converter.toEntity(productionOrder);
        productionOrderEntity.setCreatedAt(new Date());
        productionOrderEntity.setCompleted(false);
        productionOrderEntity.setCode(generateCode());

        CountingEquipmentEntity countingEquipmentEntity = countingEquipmentEntityOpt.get();
        countingEquipmentEntity.setId(productionOrder.getEquipmentId());
        productionOrderEntity.setEquipment(countingEquipmentEntity);
        productionOrderEntity.setIms(countingEquipmentEntity.getIms());

        ProductionOrderEntity persistedProductionOrder = repository.save(productionOrderEntity);

        log.info(() -> String.format("Change status to IN PROGRESS for Equipment with code [%s]", countingEquipmentEntity.getCode()));
        setOperationStatus(countingEquipmentEntity, CountingEquipmentEntity.OperationStatus.IN_PROGRESS);

        try {
            //TODO: remove to MesProtocolProcess level
            publishToPlc(persistedProductionOrder);
        } catch (MesMqttException e) {
            log.severe("Unable to publish Production Order over MQTT.");
            return Optional.empty();
        }

        ProductionOrderDto persistedProductionOrderDto = converter.toDto(persistedProductionOrder);
        return Optional.of(persistedProductionOrderDto);
    }

    @Override
    public String generateCode() {

        Optional<ProductionOrderEntity> productionOrderOpt = repository.findTopByOrderByIdDesc();
        String codePrefix = OBO_SECTION_PREFIX + CODE_PREFIX + DateUtil.getCurrentYearLastTwoDigits();

        return productionOrderOpt.isEmpty() ?
                codePrefix + String.format(FIVE_DIGIT_NUMBER_FORMAT, FIRST_CODE_VALUE) :
                codePrefix + generateFormattedCodeValue(productionOrderOpt.get(), codePrefix);
    }

    private String generateFormattedCodeValue(ProductionOrderEntity productionOrder, String codePrefix) {

        if (productionOrder.getCode() == null || productionOrder.getCode().isEmpty()) {
            String message = "Unable to generate new code: last stored Production Order code is null or empty";
            log.warning(message);
            throw new IllegalStateException(message);
        }

        String lastCodeValueAsString = productionOrder.getCode().substring(codePrefix.length());
        int lastCodeValue = Integer.parseInt(lastCodeValueAsString);

        return String.format(FIVE_DIGIT_NUMBER_FORMAT, ++lastCodeValue);
    }

    private void publishToPlc(ProductionOrderEntity productionOrderEntity) throws MesMqttException {
        ProductionOrderMqttDto productionOrderMqttDto = converter.toMqttDto(productionOrderEntity, true);
        mqttClient.publish(mqttSettings.getProtCountPlcTopic(), productionOrderMqttDto);
    }

    @Override
    public ProductionOrderEntity saveAndUpdate(ProductionOrderEntity productionOrder) {
        return repository.save(productionOrder);
    }

    @Override
    public List<ProductionOrderEntity> saveAndUpdateAll(List<ProductionOrderEntity> productionOrder) {
        return repository.saveAll(productionOrder);
    }

    @Override
    public void delete(ProductionOrderEntity productionOrder) {
        repository.delete(productionOrder);
    }

    @Override
    public Optional<ProductionOrderEntity> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<ProductionOrderDto> findDtoById(Long id) {
        Optional<ProductionOrderEntity> entity = repository.findById(id);
        if (entity.isEmpty()) {
            return Optional.empty();
        }
        ProductionOrderDto dto = converter.toDto(entity.get());
        return Optional.of(dto);
    }

    public List<Long> findExistingIds(List<Long> ids) {
        List<ProductionOrderEntity> existingEntities = repository.findByIdIn(ids);

        return existingEntities.stream()
                .map(ProductionOrderEntity::getId)
                .toList();
    }

    @Override
    public List<ProductionOrderSummaryDto> getCompletedWithoutComposed() {
        List<ProductionOrderSummaryEntity> persistedProductionOrders = repository.findCompletedWithoutComposed();
        return summaryConverter.toDto(persistedProductionOrders, ProductionOrderSummaryDto.class);
    }

    @Override
    public void setProductionOrderApproval(Long composedOrderId, boolean isApproved) {
        try {
            List<ProductionOrderEntity> productionOrders = repository.findByComposedProductionOrderId(composedOrderId);

            if (productionOrders == null || productionOrders.isEmpty()) {
                log.warning("No production orders found for composed order ID: " + composedOrderId);
                return;
            }

            for (ProductionOrderEntity productionOrder : productionOrders) {
                productionOrder.setIsApproved(isApproved);
            }

            saveAndUpdateAll(productionOrders);
        } catch (Exception e) {
            log.warning("Error in setProductionOrderApproval: " + e.getMessage());
        }
    }

    @Override
    public List<ProductionOrderDto> findByEquipmentAndPeriod(Long equipmentId, Date startDate, Date endDate) {
        List<ProductionOrderEntity> productionOrders = repository.findByEquipmentAndPeriod(equipmentId, startDate, endDate);
        return converter.toDto(productionOrders);
    }

    @Override
    public boolean isCompleted(String productionOrderCode) {
        if (productionOrderCode == null) {
            return false;
        }
        return repository.isCompleted(productionOrderCode);
    }

    @Override
    public Long calculateScheduledTimeInSeconds(Long equipmentId, Date startDate, Date endDate) {

        List<ProductionOrderEntity> productionOrders = repository.findByEquipmentAndPeriod(equipmentId, startDate, endDate);
        long totalActiveTime = 0L;
        for (ProductionOrderEntity productionOrder : productionOrders) {

            Long productionOrderActiveTime = calculateScheduledTime(productionOrder, startDate, endDate);
            totalActiveTime += productionOrderActiveTime;
        }

        return totalActiveTime;
    }

    private Long calculateScheduledTime(ProductionOrderEntity productionOrder, Date startDate, Date endDate) {
        Date createdAt = productionOrder.getCreatedAt();
        Date completedAt = productionOrder.getCompletedAt();

        startDate = (startDate.before(createdAt)) ? createdAt : startDate;
        endDate = (endDate.before(createdAt)) ? createdAt : endDate;
        endDate = (completedAt != null && completedAt.before(endDate)) ? completedAt : endDate;

        return Math.max(0, endDate.getTime() - startDate.getTime());
    }

    private CountingEquipmentDto setOperationStatus(CountingEquipmentEntity countingEquipment, CountingEquipmentEntity.OperationStatus status) {
        countingEquipmentService.setOperationStatus(countingEquipment, status);
        return equipmentConverter.toDto(countingEquipment, CountingEquipmentDto.class);
    }
}
