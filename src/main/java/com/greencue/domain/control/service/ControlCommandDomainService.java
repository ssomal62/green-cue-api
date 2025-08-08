package com.greencue.domain.control.service;

import com.greencue.domain.control.model.ControlCommand;
import com.greencue.domain.automation.model.RuleAction;

/**
 * 제어 명령 도메인 서비스 인터페이스
 */
public interface ControlCommandDomainService {
    
    /**
     * 자동화 규칙 액션으로부터 제어 명령 생성
     */
    ControlCommand createFromRuleAction(RuleAction ruleAction, Long ruleId);
    
    /**
     * 제어 명령의 유효성 검증
     */
    boolean validateCommand(ControlCommand command);
    
    /**
     * 제어 명령이 실행 가능한지 확인
     */
    boolean isExecutable(ControlCommand command);
}