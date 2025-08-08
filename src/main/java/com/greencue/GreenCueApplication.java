package com.greencue;

import com.greencue.shared.config.MqttProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(MqttProperties.class)
public class GreenCueApplication {

    public static void main(String[] args) {
        SpringApplication.run(GreenCueApplication.class, args);
    }

}
