package com.greencue.application.exception;

import com.greencue.shared.common.exception.ApplicationException;

/**
 * 스마트팜 오케스트레이션 관련 예외
 */
public class SmartFarmOrchestrationException extends ApplicationException {
    
    public SmartFarmOrchestrationException(String message) {
        super("SMART_FARM_ORCHESTRATION_ERROR", message);
    }
    
    public SmartFarmOrchestrationException(String message, Throwable cause) {
        super("SMART_FARM_ORCHESTRATION_ERROR", message, cause);
    }
}
