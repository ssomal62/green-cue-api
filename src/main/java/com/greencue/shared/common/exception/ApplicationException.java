package com.greencue.shared.common.exception;

/**
 * 애플리케이션 레벨 예외의 기본 클래스
 */
public abstract class ApplicationException extends RuntimeException {
    
    private final String errorCode;
    
    protected ApplicationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    protected ApplicationException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
