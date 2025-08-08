package com.greencue.domain.automation.api.dto;

import com.greencue.domain.automation.model.AutomationRule;

import java.time.LocalDateTime;

public record AutomationRuleResponse(
        Long id,
        String condition,
        String action,
        LocalDateTime createdAt,
        Boolean isActive
) {
    public static AutomationRuleResponse fromEntity(AutomationRule entity) {
        return new AutomationRuleResponse(
                entity.getId(),
                entity.getCondition(),
                entity.getAction(),
                entity.getCreatedAt(),
                entity.getIsActive()
        );
    }
}
