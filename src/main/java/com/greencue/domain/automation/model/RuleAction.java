package com.greencue.domain.automation.model;

import com.greencue.domain.control.model.ControlTarget;
import com.greencue.domain.control.model.CommandType;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 자동화 규칙의 액션을 나타내는 Value Object
 */
@Getter
@EqualsAndHashCode
public class RuleAction {
    private final ControlTarget target;
    private final CommandType command;
    private final Float value;

    public RuleAction(ControlTarget target, CommandType command, Float value) {
        if (target == null) {
            throw new IllegalArgumentException("제어 대상은 필수입니다.");
        }
        if (command == null) {
            throw new IllegalArgumentException("제어 명령은 필수입니다.");
        }
        if (command.requiresValue() && value == null) {
            throw new IllegalArgumentException(command.getDescription() + " 명령은 값이 필요합니다.");
        }
        if (!command.requiresValue() && value != null) {
            throw new IllegalArgumentException(command.getDescription() + " 명령은 값이 필요하지 않습니다.");
        }
        
        this.target = target;
        this.command = command;
        this.value = value;
    }

    /**
     * 문자열로부터 RuleAction 파싱 (예: "led on", "led brightness 80")
     */
    public static RuleAction fromString(String action) {
        if (action == null || action.trim().isEmpty()) {
            throw new IllegalArgumentException("액션 문자열은 필수입니다.");
        }

        String[] parts = action.trim().split("\\s+");
        if (parts.length < 2) {
            throw new IllegalArgumentException("액션 형식이 올바르지 않습니다. 예: 'led on' 또는 'led brightness 80'");
        }

        ControlTarget target = ControlTarget.fromCode(parts[0]);
        CommandType command = CommandType.fromCode(parts[1]);
        
        Float value = null;
        if (parts.length == 3) {
            value = Float.parseFloat(parts[2]);
        }

        return new RuleAction(target, command, value);
    }

    @Override
    public String toString() {
        if (value != null) {
            return target.getCode() + " " + command.getCode() + " " + value;
        } else {
            return target.getCode() + " " + command.getCode();
        }
    }
}