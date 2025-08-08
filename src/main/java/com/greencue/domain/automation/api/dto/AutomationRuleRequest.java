package com.greencue.domain.automation.api.dto;

import com.greencue.domain.automation.model.AutomationRule;

public record AutomationRuleRequest(
        String condition,
        String action,
        Boolean isActive
) {
    public AutomationRule toEntity() {
        return AutomationRule.builder()
                .condition(condition)
                .action(action)
                .isActive(isActive == null || isActive)
                .build();
    }
}
