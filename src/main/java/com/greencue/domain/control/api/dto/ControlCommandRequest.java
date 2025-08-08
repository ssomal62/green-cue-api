package com.greencue.domain.control.api.dto;

import com.greencue.domain.control.model.ControlCommand;

public record ControlCommandRequest(
        String target,
        String command,
        Float commandValue,
        Long ruleId
) {
    public ControlCommand toEntity() {
        return ControlCommand.builder()
                .target(target)
                .command(command)
                .commandValue(commandValue)
                .ruleId(ruleId)
                .build();
    }
}
