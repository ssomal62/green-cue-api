package com.greencue.domain.automation.exception;

import com.greencue.shared.common.exception.DomainException;

/**
 * 자동화 규칙 관련 예외
 */
public class InvalidAutomationRuleException extends DomainException {
    
    public InvalidAutomationRuleException(String message) {
        super("INVALID_AUTOMATION_RULE", message);
    }
    
    public InvalidAutomationRuleException(String message, Throwable cause) {
        super("INVALID_AUTOMATION_RULE", message, cause);
    }
}