package com.greencue.domain.sensor.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 센서 값과 단위를 함께 관리하는 Value Object
 */
@Getter
@EqualsAndHashCode
public class SensorValue {
    private final Float value;
    private final String unit;

    public SensorValue(Float value, String unit) {
        if (value == null) {
            throw new IllegalArgumentException("센서 값은 null일 수 없습니다.");
        }
        if (unit == null || unit.trim().isEmpty()) {
            throw new IllegalArgumentException("단위는 필수입니다.");
        }
        this.value = value;
        this.unit = unit.trim();
    }

    /**
     * 센서 타입에 따른 기본 단위로 SensorValue 생성
     */
    public static SensorValue of(Float value, SensorType sensorType) {
        String unit = switch (sensorType) {
            case LIGHT -> "lux";
            case TEMPERATURE -> "℃";
            case HUMIDITY -> "%RH";
        };
        return new SensorValue(value, unit);
    }

    @Override
    public String toString() {
        return value + " " + unit;
    }
}