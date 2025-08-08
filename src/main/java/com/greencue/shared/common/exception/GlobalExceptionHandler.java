package com.greencue.shared.common.exception;

import com.greencue.shared.common.dto.ApiResponse;
import com.greencue.domain.automation.exception.InvalidAutomationRuleException;
import com.greencue.domain.control.exception.InvalidControlCommandException;
import com.greencue.domain.sensor.exception.InvalidSensorDataException;
import com.greencue.application.exception.SmartFarmOrchestrationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 글로벌 예외 처리기
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 도메인 예외 처리
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException e) {
        log.warn("도메인 예외 발생: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), e.getErrorCode());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 자동화 규칙 관련 예외 처리
     */
    @ExceptionHandler(InvalidAutomationRuleException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidAutomationRuleException(InvalidAutomationRuleException e) {
        log.warn("자동화 규칙 예외 발생: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), e.getErrorCode());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 제어 명령 관련 예외 처리
     */
    @ExceptionHandler(InvalidControlCommandException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidControlCommandException(InvalidControlCommandException e) {
        log.warn("제어 명령 예외 발생: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), e.getErrorCode());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 센서 데이터 관련 예외 처리
     */
    @ExceptionHandler(InvalidSensorDataException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidSensorDataException(InvalidSensorDataException e) {
        log.warn("센서 데이터 예외 발생: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), e.getErrorCode());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 스마트팜 오케스트레이션 관련 예외 처리
     */
    @ExceptionHandler(SmartFarmOrchestrationException.class)
    public ResponseEntity<ApiResponse<Void>> handleSmartFarmOrchestrationException(SmartFarmOrchestrationException e) {
        log.warn("스마트팜 오케스트레이션 예외 발생: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), e.getErrorCode());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 애플리케이션 예외 처리
     */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse<Void>> handleApplicationException(ApplicationException e) {
        log.warn("애플리케이션 예외 발생: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), e.getErrorCode());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 일반적인 IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("잘못된 인수 예외: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), "INVALID_ARGUMENT");
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * MethodArgumentNotValidException 처리 (Validation 실패)
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(org.springframework.web.bind.MethodArgumentNotValidException e) {
        log.warn("입력값 검증 실패: {}", e.getMessage());
        
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("입력값이 올바르지 않습니다.");
        
        ApiResponse<Void> response = ApiResponse.error(errorMessage, "VALIDATION_ERROR");
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * HttpMessageNotReadableException 처리 (JSON 파싱 실패)
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(org.springframework.http.converter.HttpMessageNotReadableException e) {
        log.warn("요청 본문 파싱 실패: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error("요청 본문 형식이 올바르지 않습니다.", "INVALID_REQUEST_BODY");
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 예상하지 못한 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception e) {
        log.error("예상하지 못한 예외 발생", e);
        
        ApiResponse<Void> response = ApiResponse.error(
            "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", 
            "INTERNAL_SERVER_ERROR"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}