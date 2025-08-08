package com.greencue.shared.infrastructure.mqtt;

import com.greencue.shared.config.MqttProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.mqtt.MqttClientConnection;
import software.amazon.awssdk.crt.mqtt.MqttMessage;
import software.amazon.awssdk.crt.mqtt.QualityOfService;
import software.amazon.awssdk.iot.AwsIotMqttConnectionBuilder;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttService {

    private final MqttProperties mqttProperties;
    private MqttClientConnection connection;

    @PostConstruct
    public void init() {
        try {
            EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
            HostResolver hostResolver = new HostResolver(eventLoopGroup);
            ClientBootstrap clientBootstrap = new ClientBootstrap(eventLoopGroup, hostResolver);

            AwsIotMqttConnectionBuilder builder = AwsIotMqttConnectionBuilder.newMtlsBuilderFromPath(
                    mqttProperties.getCertPath(),
                    mqttProperties.getKeyPath()
            );

            builder.withClientId(mqttProperties.getClientId())
                    .withEndpoint(mqttProperties.getEndpoint())
                    .withCertificateAuthorityFromPath(null, mqttProperties.getCaPath())
                    .withCleanSession(true)
                    .withBootstrap(clientBootstrap); // ← 이 줄이 필수!!

            this.connection = builder.build();
            builder.close();

            connection.connect().join();
            log.info("MQTT 연결 완료");

        } catch (Exception e) {
            log.error("MQTT 자동 연결 실패", e);
        }
    }


    public void publish(String topic, String payload) {
        if (connection == null) {
            throw new IllegalStateException("MQTT 연결되지 않음");
        }

        connection.publish(
                new MqttMessage(topic, payload.getBytes(StandardCharsets.UTF_8), QualityOfService.AT_LEAST_ONCE, false)
        ).join();

        log.info("MQTT 발행 - topic: {}, payload: {}", topic, payload);
    }

    public void subscribe(String topic, Consumer<String> messageHandler) {
        if (connection == null) {
            throw new IllegalStateException("MQTT 연결되지 않음");
        }

        connection.subscribe(
                topic,
                QualityOfService.AT_LEAST_ONCE,
                message -> {
                    String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                    messageHandler.accept(payload);
                }
        ).join();
    }

    public void disconnect() {
        if (connection != null) {
            connection.disconnect().join();
            connection.close();
            log.info("MQTT 연결 해제 완료");
        }
    }
}
