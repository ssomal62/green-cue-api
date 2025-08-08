package com.greencue.domain.sensor.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_data")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; // 'light', 'temp', 'humi'

    @Column(name = "sensor_value", nullable = false)
    private Float sensorValue;

    @Column(nullable = false)
    private String unit; // 'lux', '℃', '%RH'

    @Column(name = "device_id")
    private String deviceId; // 센서 식별자

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
