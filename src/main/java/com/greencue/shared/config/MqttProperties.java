package com.greencue.shared.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Getter
@Setter
@ConfigurationProperties(prefix = "mqtt")
public class MqttProperties {
    private boolean enabled;
    private String clientId;
    private String certPath;
    private String keyPath;
    private String caPath;
    private String endpoint;
    private Topics topics;

    @Getter
    @Setter
    public static class Topics {
        private String sensorData;
        private String controlCommand;
        private String ledControl;
        private String fanControl;
        private String automation;
        private String status;
    }
}
