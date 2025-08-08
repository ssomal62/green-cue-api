package com.greencue.domain.control.model;

/**
 * 제어 대상을 나타내는 Value Object
 */
public enum ControlTarget {
    LED("led", "LED 조명"),
    FAN("fan", "환풍기"),
    PUMP("pump", "물펌프"),
    HEATER("heater", "히터");

    private final String code;
    private final String description;

    ControlTarget(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ControlTarget fromCode(String code) {
        for (ControlTarget target : values()) {
            if (target.code.equals(code)) {
                return target;
            }
        }
        throw new IllegalArgumentException("Unknown control target code: " + code);
    }
}