package com.greencue.domain.automation.service;

import com.greencue.domain.control.service.ControlCommandService;
import com.greencue.domain.automation.api.dto.AutomationRuleRequest;
import com.greencue.domain.automation.api.dto.AutomationRuleResponse;
import com.greencue.domain.control.api.dto.ControlCommandRequest;
import com.greencue.domain.automation.model.AutomationRule;
import com.greencue.domain.automation.repository.AutomationRuleRepository;
import com.greencue.domain.automation.exception.InvalidAutomationRuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutomationRuleService {

    private final AutomationRuleRepository automationRuleRepository;
    private final ControlCommandService controlCommandService;

    // 자동화 규칙 생성
    public AutomationRuleResponse createRule(AutomationRuleRequest dto) {
        AutomationRule rule = dto.toEntity();

        AutomationRule saved = automationRuleRepository.save(rule);
        log.info("자동화 규칙 생성: {} → {}", dto.condition(), dto.action());

        return AutomationRuleResponse.fromEntity(saved);
    }

    // 활성화된 규칙 조회
    public List<AutomationRuleResponse> getActiveRules() {
        List<AutomationRule> rules = automationRuleRepository.findByIsActiveTrue();
        return rules.stream()
                .map(AutomationRuleResponse::fromEntity)
                .toList();
    }

    // 규칙 활성화/비활성화
    public AutomationRuleResponse toggleRule(Long ruleId, boolean isActive) {
        AutomationRule rule = automationRuleRepository.findById(ruleId)
                .orElseThrow(() -> new InvalidAutomationRuleException("규칙을 찾을 수 없습니다: " + ruleId));

        rule.setIsActive(isActive);
        AutomationRule saved = automationRuleRepository.save(rule);

        log.info("규칙 상태 변경: {} → {}", ruleId, isActive);

        return AutomationRuleResponse.fromEntity(saved);
    }

    // 센서 데이터 기반 규칙 평가 및 실행
    public void evaluateRules(String sensorType, float sensorValue) {
        List<AutomationRule> activeRules = automationRuleRepository.findByIsActiveTrue();

        for (AutomationRule rule : activeRules) {
            if (evaluateCondition(rule.getCondition(), sensorType, sensorValue)) {
                executeAction(rule);
            }
        }
    }

    // 조건 평가 (간단한 파싱)
    private boolean evaluateCondition(String condition, String sensorType, float sensorValue) {
        try {
            // 예: "light < 200" 또는 "temp > 30"
            String[] parts = condition.trim().split("\\s+");
            if (parts.length != 3) return false;

            String conditionSensorType = parts[0];
            String operator = parts[1];
            float threshold = Float.parseFloat(parts[2]);

            if (!conditionSensorType.equals(sensorType)) return false;

            switch (operator) {
                case "<":
                    return sensorValue < threshold;
                case "<=":
                    return sensorValue <= threshold;
                case ">":
                    return sensorValue > threshold;
                case ">=":
                    return sensorValue >= threshold;
                case "==", "=":
                    return sensorValue == threshold;
                default:
                    return false;
            }
        } catch (Exception e) {
            log.error("조건 평가 실패: {}", condition, e);
            return false;
        }
    }

    // 액션 실행
    private void executeAction(AutomationRule rule) {
        String action = rule.getAction();
        String[] parts = action.trim().split("\\s+");

        if (parts.length < 2) return;

        String target = parts[0]; // "led" 또는 "fan"
        String command = parts[1]; // "on", "off", "brightness"
        Float commandValue = null;

        if (parts.length > 2 && "brightness".equals(command)) {
            try {
                commandValue = Float.parseFloat(parts[2]);
            } catch (NumberFormatException e) {
                log.error("밝기 값 파싱 실패: {}", parts[2]);
            }
        }

        try {
            ControlCommandRequest commandDto = new ControlCommandRequest(
                    target,
                    command,
                    commandValue,
                    rule.getId()
            );

            controlCommandService.executeCommand(commandDto);

            log.info("자동화 규칙 실행: {} → {} {} {}",
                    rule.getCondition(), target, command, commandDto.commandValue());

        } catch (Exception e) {
            log.error("액션 실행 실패: {}", rule.getAction(), e);
        }
    }


    // 자연어 명령을 규칙으로 변환 (간단한 예시)
    public AutomationRuleResponse createRuleFromNaturalLanguage(String naturalCommand) {
        // 예: "조도가 200 lux 이하일 때 LED 켜줘"
        String condition = "";
        String action = "";

        if (naturalCommand.contains("조도") && naturalCommand.contains("이하")) {
            // 간단한 파싱 로직 (실제로는 GPT API 사용)
            condition = "light < 200";
            action = "led on";
        } else if (naturalCommand.contains("온도") && naturalCommand.contains("이상")) {
            condition = "temp > 30";
            action = "fan on";
        } else {
            throw new InvalidAutomationRuleException("지원하지 않는 자연어 명령 형식입니다: " + naturalCommand);
        }

        AutomationRuleRequest request = new AutomationRuleRequest(condition, action, true);
        return createRule(request);
    }

    /**
     * 센서 데이터에 의해 트리거되는 자동화 규칙들을 찾습니다.
     * @param sensorData 센서 데이터
     * @return 트리거된 규칙들의 리스트
     */
    public List<AutomationRule> findTriggeredRules(com.greencue.domain.sensor.model.SensorData sensorData) {
        List<AutomationRule> activeRules = automationRuleRepository.findByIsActiveTrue();

        return activeRules.stream()
                .filter(rule -> evaluateConditionForSensorData(rule.getCondition(), sensorData))
                .toList();
    }

    /**
     * 센서 데이터에 대해 조건을 평가합니다.
     * @param condition 조건 문자열 (예: "light < 200", "temp > 30")
     * @param sensorData 센서 데이터
     * @return 조건이 만족되면 true
     */
    private boolean evaluateConditionForSensorData(String condition, com.greencue.domain.sensor.model.SensorData sensorData) {
        try {
            // 조건 파싱: "sensorType operator threshold"
            String[] parts = condition.trim().split("\\s+");
            if (parts.length != 3) return false;

            String conditionSensorType = parts[0];
            String operator = parts[1];
            float threshold = Float.parseFloat(parts[2]);

            // 센서 타입 매칭
            if (!conditionSensorType.equals(sensorData.getType())) {
                return false;
            }

            // 센서 값 추출
            float sensorValue = sensorData.getSensorValue();

            // 조건 평가
            switch (operator) {
                case "<":
                    return sensorValue < threshold;
                case "<=":
                    return sensorValue <= threshold;
                case ">":
                    return sensorValue > threshold;
                case ">=":
                    return sensorValue >= threshold;
                case "==", "=":
                    return sensorValue == threshold;
                default:
                    log.warn("지원하지 않는 연산자: {}", operator);
                    return false;
            }
        } catch (Exception e) {
            log.error("조건 평가 실패: condition={}, sensorData={}", condition, sensorData, e);
            return false;
        }
    }
}
