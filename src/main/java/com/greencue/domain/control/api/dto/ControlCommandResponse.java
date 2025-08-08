package com.greencue.domain.control.api.dto;

import com.greencue.domain.control.model.ControlCommand;

import java.time.LocalDateTime;

public record ControlCommandResponse(
        Long id,
        String target,
        String command,
        Float commandValue,
        LocalDateTime createdAt,
        Long ruleId
) {
    public static ControlCommandResponse fromEntity(ControlCommand entity) {
        return new ControlCommandResponse(
                entity.getId(),
                entity.getTarget(),
                entity.getCommand(),
                entity.getCommandValue(),
                entity.getCreatedAt(),
                entity.getRuleId()
        );
    }
}
