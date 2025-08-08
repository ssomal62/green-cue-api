package com.greencue.application.service;

import com.greencue.domain.sensor.model.SensorData;
import com.greencue.domain.automation.model.AutomationRule;
import com.greencue.domain.control.model.ControlCommand;
import com.greencue.domain.automation.service.AutomationRuleService;
import com.greencue.domain.control.service.ControlCommandService;
import com.greencue.domain.control.exception.InvalidControlCommandException;
import com.greencue.application.exception.SmartFarmOrchestrationException;
import com.greencue.shared.infrastructure.mqtt.MqttControlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 스마트팜 전체 오케스트레이션을 담당하는 애플리케이션 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SmartFarmOrchestrationService {
    
    private final AutomationRuleService automationRuleService;
    private final ControlCommandService controlCommandService;
    private final MqttControlService mqttControlService;

    /**
     * 센서 데이터 수신 시 자동화 규칙 실행
     */
    @Transactional
    public void processSensorData(SensorData sensorData) {
        log.info("센서 데이터 처리 시작: {}", sensorData);
        
        try {
            // 1. 활성화된 자동화 규칙 조회
            List<AutomationRule> triggeredRules = automationRuleService.findTriggeredRules(sensorData);
            
            if (triggeredRules.isEmpty()) {
                log.debug("트리거된 자동화 규칙이 없습니다.");
                return;
            }
            
            // 2. 각 규칙에 대해 제어 명령 실행
            for (AutomationRule rule : triggeredRules) {
                executeAutomationRule(rule, sensorData);
            }
            
        } catch (Exception e) {
            log.error("센서 데이터 처리 중 오류 발생", e);
            throw new SmartFarmOrchestrationException("센서 데이터 처리 실패", e);
        }
    }

    /**
     * 자동화 규칙 실행
     */
    private void executeAutomationRule(AutomationRule rule, SensorData sensorData) {
        log.info("자동화 규칙 실행: {} (센서 데이터: {})", rule.getId(), sensorData.getId());
        
        try {
            // 1. 제어 명령 생성 및 저장
            ControlCommand command = controlCommandService.createFromAutomationRule(rule);
            
            // 2. MQTT를 통한 실제 장치 제어
            mqttControlService.sendControlCommand(command);
            
            log.info("자동화 규칙 실행 완료: 규칙 ID={}, 명령 ID={}", rule.getId(), command.getId());
            
        } catch (Exception e) {
            log.error("자동화 규칙 실행 실패: 규칙 ID={}", rule.getId(), e);
            // 규칙 실행 실패 시에도 다른 규칙은 계속 실행되도록 예외를 다시 던지지 않음
        }
    }

    /**
     * 수동 제어 명령 실행
     */
    @Transactional
    public ControlCommand executeManualControl(String target, String command, Float value) {
        log.info("수동 제어 명령 실행: {} {} {}", target, command, value);
        
        try {
            // 1. 제어 명령 생성 및 저장
            ControlCommand controlCommand = controlCommandService.createManualCommand(target, command, value);
            
            // 2. MQTT를 통한 실제 장치 제어
            mqttControlService.sendControlCommand(controlCommand);
            
            log.info("수동 제어 명령 실행 완료: 명령 ID={}", controlCommand.getId());
            return controlCommand;
            
        } catch (Exception e) {
            log.error("수동 제어 명령 실행 실패", e);
            throw new SmartFarmOrchestrationException("제어 명령 실행 실패", e);
        }
    }
}