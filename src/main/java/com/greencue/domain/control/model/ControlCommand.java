package com.greencue.domain.control.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "control_command")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ControlCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String target; // 'led', 'fan'

    @Column(nullable = false)
    private String command; // 'on', 'off', 'brightness'

    @Column(name = "command_value")
    private Float commandValue; // (밝기 등, 옵션)

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "rule_id")
    private Long ruleId; // 자동화 규칙 참조
}
