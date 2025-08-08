package com.greencue.domain.sensor.api.dto;

import com.greencue.domain.sensor.model.SensorData;

public record SensorDataRequest(
        String type,
        Float sensorValue,
        String unit,
        String deviceId
) {
    public SensorData toEntity() {
        return SensorData.builder()
                .type(type)
                .sensorValue(sensorValue)
                .unit(unit)
                .deviceId(deviceId)
                .build();
    }
}
