package com.greencue.domain.control.model;

/**
 * 제어 명령 타입을 나타내는 Value Object
 */
public enum CommandType {
    ON("on", "켜기"),
    OFF("off", "끄기"),
    BRIGHTNESS("brightness", "밝기 조절"),
    SPEED("speed", "속도 조절"),
    TEMPERATURE("temperature", "온도 설정");

    private final String code;
    private final String description;

    CommandType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static CommandType fromCode(String code) {
        for (CommandType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown command type code: " + code);
    }

    /**
     * 명령 타입이 값이 필요한지 확인
     */
    public boolean requiresValue() {
        return this == BRIGHTNESS || this == SPEED || this == TEMPERATURE;
    }
}