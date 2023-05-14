package com.tde.mescloud.protocol;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tde.mescloud.api.mqtt.MqttClient;
import com.tde.mescloud.exception.MesMqttException;
import com.tde.mescloud.model.dto.HasReceivedMqttDto;
import com.tde.mescloud.model.dto.MqttDto;
import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.logging.Level;

@Component(CountProtocol.BEAN_NAME)
@Log
public class CountProtocol extends AbstractMesProtocol {

    public final static String BEAN_NAME = "protCountService";

    private final ObjectMapper objectMapper;
    private final MqttClient mqttClient;
    private final MesMqttSettings mesMqttSettings;

    public CountProtocol(ObjectMapper objectMapper, MqttClient mqttClient, MesMqttSettings mesMqttSettings) {
        this.objectMapper = objectMapper;
        this.mqttClient = mqttClient;
        this.mesMqttSettings = mesMqttSettings;
    }


    @PostConstruct
    private void subscribeProtocolTopic() {
        try {
            mqttClient.subscribe(mesMqttSettings.getProtCountBackendTopic());
        } catch (MesMqttException e) {
            log.log(Level.SEVERE, e, () -> String.format("Unable to subscribe to [%s] protocol topic [%s]",
                    this.getClass().getName(), mesMqttSettings.getProtCountBackendTopic()));
            throw new RuntimeException(e);
        }
    }

    @Override
    public void react(AWSIotMessage message) {

        log.info(() -> String.format("Message delegated to [%s] on topic [%s] with payload: [%s]",
                this.getClass().toString(), message.getTopic(), message.getStringPayload()));

        Optional<MqttDto> optMqttDTO = parseMqttDTO(message);
        if(optMqttDTO.isEmpty()) {
            publishNotReceived();
            return;
        }

        MqttDto mqttDTO = optMqttDTO.get();
        publishHasReceived(mqttDTO);
        executeMesProcess(mqttDTO);
    }

    private Optional<MqttDto> parseMqttDTO(AWSIotMessage message) {
        try {
            MqttDto mqttDTO = objectMapper.readValue(message.getStringPayload(), MqttDto.class);
            return Optional.of(mqttDTO);
        } catch (JsonMappingException e) {
            log.log(Level.SEVERE, e, () -> String.format("Unable to map JSON [%s]", message.getStringPayload()));
            return Optional.empty();
        } catch (JsonProcessingException e) {
            log.log(Level.SEVERE, e, () -> String.format("Unable to deserialize JSON [%s]", message.getStringPayload()));
            return Optional.empty();
        }
    }

    private void publishNotReceived() {
        try {
            log.severe("Unable to parse a valid MqttDTO from payload.");
            mqttClient.publish(mesMqttSettings.getProtCountPlcTopic(), new HasReceivedMqttDto(false));
        } catch (MesMqttException e) {
            log.log(Level.SEVERE,"Failed to publish Not Received message", e);
            throw new RuntimeException(e);
        }
    }

    private void publishHasReceived(MqttDto mqttDTO) {
        try {
            HasReceivedMqttDto hasReceivedMqttDTO = new HasReceivedMqttDto(mqttDTO.getEquipmentCode(), true);
            mqttClient.publish(mesMqttSettings.getProtCountPlcTopic(), hasReceivedMqttDTO);
        } catch (MesMqttException e) {
            log.log(Level.SEVERE, e, () -> String.format("Failed to publish Has Received message as a response to [%s] for equipment [%s]",
                    mqttDTO.getJsonType(), mqttDTO.getEquipmentCode()));
            throw new RuntimeException(e);
        }
    }
}