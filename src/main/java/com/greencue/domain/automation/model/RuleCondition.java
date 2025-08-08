package com.greencue.domain.automation.model;

import com.greencue.domain.sensor.model.SensorType;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 자동화 규칙의 조건을 나타내는 Value Object
 */
@Getter
@EqualsAndHashCode
public class RuleCondition {
    private final SensorType sensorType;
    private final ComparisonOperator operator;
    private final Float threshold;

    public RuleCondition(SensorType sensorType, ComparisonOperator operator, Float threshold) {
        if (sensorType == null) {
            throw new IllegalArgumentException("센서 타입은 필수입니다.");
        }
        if (operator == null) {
            throw new IllegalArgumentException("비교 연산자는 필수입니다.");
        }
        if (threshold == null) {
            throw new IllegalArgumentException("임계값은 필수입니다.");
        }
        
        this.sensorType = sensorType;
        this.operator = operator;
        this.threshold = threshold;
    }

    /**
     * 문자열로부터 RuleCondition 파싱 (예: "light < 200")
     */
    public static RuleCondition fromString(String condition) {
        if (condition == null || condition.trim().isEmpty()) {
            throw new IllegalArgumentException("조건 문자열은 필수입니다.");
        }

        String[] parts = condition.trim().split("\\s+");
        if (parts.length != 3) {
            throw new IllegalArgumentException("조건 형식이 올바르지 않습니다. 예: 'light < 200'");
        }

        SensorType sensorType = SensorType.fromCode(parts[0]);
        ComparisonOperator operator = ComparisonOperator.fromSymbol(parts[1]);
        Float threshold = Float.parseFloat(parts[2]);

        return new RuleCondition(sensorType, operator, threshold);
    }

    /**
     * 현재 센서 값이 조건을 만족하는지 확인
     */
    public boolean isSatisfied(Float currentValue) {
        if (currentValue == null) {
            return false;
        }
        
        return operator.compare(currentValue, threshold);
    }

    @Override
    public String toString() {
        return sensorType.getCode() + " " + operator.getSymbol() + " " + threshold;
    }

    public enum ComparisonOperator {
        LESS_THAN("<"),
        LESS_THAN_OR_EQUAL("<="),
        GREATER_THAN(">"),
        GREATER_THAN_OR_EQUAL(">="),
        EQUAL("==");

        private final String symbol;

        ComparisonOperator(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }

        public boolean compare(Float left, Float right) {
            return switch (this) {
                case LESS_THAN -> left < right;
                case LESS_THAN_OR_EQUAL -> left <= right;
                case GREATER_THAN -> left > right;
                case GREATER_THAN_OR_EQUAL -> left >= right;
                case EQUAL -> left.equals(right);
            };
        }

        public static ComparisonOperator fromSymbol(String symbol) {
            for (ComparisonOperator op : values()) {
                if (op.symbol.equals(symbol)) {
                    return op;
                }
            }
            throw new IllegalArgumentException("Unknown comparison operator: " + symbol);
        }
    }
}