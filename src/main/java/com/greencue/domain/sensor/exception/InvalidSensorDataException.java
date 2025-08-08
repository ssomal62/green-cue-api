package com.greencue.domain.sensor.exception;

import com.greencue.shared.common.exception.DomainException;

/**
 * 센서 데이터 관련 예외
 */
public class InvalidSensorDataException extends DomainException {
    
    public InvalidSensorDataException(String message) {
        super("INVALID_SENSOR_DATA", message);
    }
    
    public InvalidSensorDataException(String message, Throwable cause) {
        super("INVALID_SENSOR_DATA", message, cause);
    }
}