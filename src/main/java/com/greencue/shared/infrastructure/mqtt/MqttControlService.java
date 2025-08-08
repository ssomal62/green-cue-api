package com.greencue.shared.infrastructure.mqtt;

import com.greencue.domain.control.api.dto.ControlCommandRequest;
import com.greencue.domain.control.model.ControlCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttControlService {

    private final MqttService mqttService;

    public void sendControlCommand(ControlCommandRequest dto) {
        String topic = String.format("smartfarm/control/%s/%s", dto.target(), dto.command());
        String payload = dto.commandValue() != null ? dto.commandValue().toString() : "";

        mqttService.publish(topic, payload);
        log.info("제어 명령 발행: topic={}, payload={}", topic, payload);
    }

    public void sendControlCommand(ControlCommand command) {
        String topic = String.format("smartfarm/control/%s/%s",
                command.getTarget().toLowerCase(),
                command.getCommand().toLowerCase());

        String payload = command.getCommandValue() != null ? command.getCommandValue().toString() : "1";

        mqttService.publish(topic, payload);
        log.info("제어 명령 발행: topic={}, payload={}, commandId={}", topic, payload, command.getId());
    }
}
