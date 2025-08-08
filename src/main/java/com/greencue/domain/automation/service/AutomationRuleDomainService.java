package com.greencue.domain.automation.service;

import com.greencue.domain.automation.model.AutomationRule;
import com.greencue.domain.automation.model.RuleCondition;
import com.greencue.domain.sensor.model.SensorData;

import java.util.List;

/**
 * 자동화 규칙 도메인 서비스 인터페이스
 */
public interface AutomationRuleDomainService {
    
    /**
     * 센서 데이터가 자동화 규칙을 트리거하는지 확인
     */
    List<AutomationRule> findTriggeredRules(SensorData sensorData);
    
    /**
     * 자동화 규칙의 유효성 검증
     */
    boolean validateRule(AutomationRule rule);
    
    /**
     * 규칙 조건 파싱 및 검증
     */
    RuleCondition parseAndValidateCondition(String conditionString);
}