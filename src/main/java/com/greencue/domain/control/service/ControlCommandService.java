package com.greencue.domain.control.service;

import com.greencue.domain.control.api.dto.ControlCommandRequest;
import com.greencue.domain.control.api.dto.ControlCommandResponse;
import com.greencue.domain.control.model.ControlCommand;
import com.greencue.domain.control.repository.ControlCommandRepository;
import com.greencue.domain.control.exception.InvalidControlCommandException;
import com.greencue.shared.infrastructure.mqtt.MqttControlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ControlCommandService {

    private final ControlCommandRepository controlCommandRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private MqttControlService mqttControlService; // Optional - MQTT가 비활성화되면 null


    // MQTT 서비스가 있을 때만 주입
    @Autowired(required = false)
    public void setMqttControlService(MqttControlService mqttControlService) {
        this.mqttControlService = mqttControlService;
    }

    // 제어 명령 실행
    public ControlCommandResponse executeCommand(ControlCommandRequest dto) {
        ControlCommand command = dto.toEntity();

        ControlCommand saved = controlCommandRepository.save(command);

        // MQTT로 제어 명령 발행 (MQTT가 활성화된 경우에만)
        if (mqttControlService != null) {
            mqttControlService.sendControlCommand(dto);
        } else {
            log.warn("MQTT 서비스가 비활성화되어 있어 제어 명령을 발행할 수 없습니다.");
        }

        // WebSocket으로 실시간 전송
        messagingTemplate.convertAndSend("/topic/control-command", ControlCommandResponse.fromEntity(saved));

        log.info("제어 명령 실행: {} {} {}", dto.target(), dto.command(), dto.commandValue());

        return ControlCommandResponse.fromEntity(saved);
    }

    // LED 밝기 조절
    public ControlCommandResponse setLedBrightness(float brightness) {
        ControlCommandRequest request = new ControlCommandRequest(
                "led", "brightness", brightness, null);


        return executeCommand(request);
    }

    // LED ON/OFF
    public ControlCommandResponse setLedPower(boolean isOn) {
        ControlCommandRequest request = new ControlCommandRequest(
                "led", isOn ? "on" : "off", null, null);

        return executeCommand(request);
    }

    // 팬 ON/OFF
    public ControlCommandResponse setFanPower(boolean isOn) {
        ControlCommandRequest request = new ControlCommandRequest(
                "fan", isOn ? "on" : "off", null, null);

        return executeCommand(request);
    }

    // 타겟별 최근 명령 조회
    public List<ControlCommandResponse> getRecentCommandsByTarget(String target, int limit) {
        List<ControlCommand> commands = controlCommandRepository.findTop10ByTargetOrderByCreatedAtDesc(target);
        return commands.stream()
                .limit(limit)
                .map(ControlCommandResponse::fromEntity)
                .toList();
    }

    // 규칙에 의한 명령 조회
    public List<ControlCommandResponse> getCommandsByRule(Long ruleId) {
        List<ControlCommand> commands = controlCommandRepository.findByRuleIdOrderByCreatedAtDesc(ruleId);
        return commands.stream()
                .map(ControlCommandResponse::fromEntity)
                .toList();
    }

    /**
     * 자동화 규칙으로부터 제어 명령을 생성합니다.
     * @param rule 자동화 규칙
     * @return 생성된 제어 명령
     */
    public ControlCommand createFromAutomationRule(com.greencue.domain.automation.model.AutomationRule rule) {
        String action = rule.getAction();
        String[] parts = action.trim().split("\\s+");

        if (parts.length < 2) {
            throw new InvalidControlCommandException("잘못된 액션 형식: " + action);
        }

        String target = parts[0]; // "led" 또는 "fan"
        String command = parts[1]; // "on", "off", "brightness"
        Float commandValue = null;

        if (parts.length > 2 && "brightness".equals(command)) {
            commandValue = parseCommandValue(parts[2]);  // 파싱 로직 분리
        }

        try {
            ControlCommand controlCommand = ControlCommand.builder()
                    .target(target)
                    .command(command)
                    .commandValue(commandValue)
                    .ruleId(rule.getId())
                    .build();

            ControlCommand saved = controlCommandRepository.save(controlCommand);
            log.info("자동화 규칙으로부터 제어 명령 생성: 규칙 ID={}, 명령 ID={}", rule.getId(), saved.getId());

            return saved;

        } catch (Exception e) {
            log.error("자동화 규칙으로부터 제어 명령 생성 실패: 규칙 ID={}", rule.getId(), e);
            throw new InvalidControlCommandException("제어 명령 생성 실패", e);
        }
    }

    private Float parseCommandValue(String raw) {
        try {
            return Float.parseFloat(raw);
        } catch (NumberFormatException e) {
            log.error("밝기 값 파싱 실패: {}", raw);
            throw new InvalidControlCommandException("잘못된 밝기 값: " + raw);
        }
    }


    /**
     * 수동 제어 명령을 생성합니다.
     * @param target 제어 대상 (led, fan)
     * @param command 제어 명령 (on, off, brightness)
     * @param value 제어 값 (밝기 조절 시 사용)
     * @return 생성된 제어 명령
     */
    public ControlCommand createManualCommand(String target, String command, Float value) {
        try {
            ControlCommand controlCommand = ControlCommand.builder()
                    .target(target)
                    .command(command)
                    .commandValue(value)
                    .ruleId(null) // 수동 명령이므로 규칙 ID는 null
                    .build();

            ControlCommand saved = controlCommandRepository.save(controlCommand);
            log.info("수동 제어 명령 생성: {} {} {}", target, command, value);

            return saved;

        } catch (Exception e) {
            log.error("수동 제어 명령 생성 실패: {} {} {}", target, command, value, e);
            throw new InvalidControlCommandException("제어 명령 생성 실패", e);
        }
    }
}
