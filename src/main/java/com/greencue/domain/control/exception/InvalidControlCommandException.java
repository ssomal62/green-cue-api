package com.greencue.domain.control.exception;

import com.greencue.shared.common.exception.DomainException;

/**
 * 제어 명령 관련 예외
 */
public class InvalidControlCommandException extends DomainException {
    
    public InvalidControlCommandException(String message) {
        super("INVALID_CONTROL_COMMAND", message);
    }
    
    public InvalidControlCommandException(String message, Throwable cause) {
        super("INVALID_CONTROL_COMMAND", message, cause);
    }
}