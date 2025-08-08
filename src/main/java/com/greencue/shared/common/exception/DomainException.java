package com.greencue.shared.common.exception;

/**
 * 도메인 레벨 예외의 기본 클래스
 */
public abstract class DomainException extends RuntimeException {
    
    private final String errorCode;
    
    protected DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    protected DomainException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}