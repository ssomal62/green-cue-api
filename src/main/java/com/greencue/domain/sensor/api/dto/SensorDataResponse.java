package com.greencue.domain.sensor.api.dto;

import com.greencue.domain.sensor.model.SensorData;

import java.time.LocalDateTime;

public record SensorDataResponse(
        Long id,
        String type,
        Float sensorValue,
        String unit,
        String deviceId,
        LocalDateTime createdAt
) {
    public static SensorDataResponse fromEntity(SensorData entity) {
        return new SensorDataResponse(
                entity.getId(),
                entity.getType(),
                entity.getSensorValue(),
                entity.getUnit(),
                entity.getDeviceId(),
                entity.getCreatedAt()
        );
    }
}
