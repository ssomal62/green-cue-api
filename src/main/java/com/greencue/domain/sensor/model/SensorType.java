package com.greencue.domain.sensor.model;

/**
 * 센서 타입을 나타내는 Value Object
 */
public enum SensorType {
    LIGHT("light", "조도"),
    TEMPERATURE("temp", "온도"),
    HUMIDITY("humi", "습도");

    private final String code;
    private final String description;

    SensorType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static SensorType fromCode(String code) {
        for (SensorType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown sensor type code: " + code);
    }

    public static SensorType fromMqttType(String mqttType) {
        if (mqttType == null) return null;
        return switch (mqttType.toLowerCase()) {
            case "temperature" -> TEMPERATURE;
            case "humidity" -> HUMIDITY;
            case "light" -> LIGHT;
            default -> throw new IllegalArgumentException("Unknown mqtt sensor type: " + mqttType);
        };
    }
}
