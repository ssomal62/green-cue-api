package com.greencue.shared.infrastructure.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greencue.domain.sensor.api.dto.SensorDataRequest;
import com.greencue.domain.sensor.model.SensorType;
import com.greencue.domain.sensor.service.SensorDataService;
import com.greencue.shared.config.MqttProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class MqttMessageHandler {

    private static final String SENSOR_TOPIC_PREFIX = "smartfarm/sensor/";
    private static final String SENSOR_TOPIC_SUFFIX = "/data";

    private static final String KEY_VALUE = "value";
    private static final String KEY_UNIT = "unit";
    private static final String KEY_DEVICE_ID = "device_id";
    private static final String KEY_STATUS = "status";

    private final MqttService mqttService;
    private final MqttProperties mqttProperties;
    private final SensorDataService sensorDataService;
    private final ObjectMapper objectMapper;

    // 토픽별 센서 타입 맵
    private final Map<String, String> topicToSensorType = new ConcurrentHashMap<>();

    // 센서 타입 목록 (MQTT 수신 기준 이름)
    private static final String[] SENSOR_TYPES = {"temperature", "humidity", "light"};

    @PostConstruct
    public void init() {
        // 센서 데이터 토픽 구독
        subscribeToSensorTopics();

        // 상태 업데이트 토픽 구독
        mqttService.subscribe(mqttProperties.getTopics().getStatus(), this::handleStatusUpdate);

        log.info("MQTT 메시지 핸들러 초기화 완료");
    }

    private void subscribeToSensorTopics() {
        for (String sensorType : SENSOR_TYPES) {
            String topic = SENSOR_TOPIC_PREFIX + sensorType + SENSOR_TOPIC_SUFFIX;
            mqttService.subscribe(topic, payload -> handleSensorData(payload, sensorType));
            topicToSensorType.put(topic, sensorType);
            log.info("센서 토픽 구독: {}", topic);
        }
    }

    private void handleSensorData(String payload, String mqttSensorType) {
        try {
            log.debug("센서 데이터 수신 [{}]: {}", mqttSensorType, payload);

            Map<String, Object> data = objectMapper.readValue(payload, Map.class);

            Float value = getFloatValue(data.get(KEY_VALUE));
            String unit = (String) data.get(KEY_UNIT);
            String deviceId = (String) data.get(KEY_DEVICE_ID);

            if (value != null) {
                SensorType sensorType = SensorType.fromMqttType(mqttSensorType);

                SensorDataRequest sensorDataRequest = new SensorDataRequest(
                        sensorType.getCode(), // "temp", "humi", "light"
                        value,
                        unit,
                        deviceId
                );

                sensorDataService.saveSensorData(sensorDataRequest);
                log.info("센서 데이터 저장 완료: {} = {} {}", sensorType.name(), value, unit);
            } else {
                log.warn("센서 데이터 파싱 실패: {}", payload);
            }

        } catch (Exception e) {
            log.error("센서 데이터 처리 중 오류 [{}]: {}", mqttSensorType, e.getMessage(), e);
        }
    }

    private void handleStatusUpdate(String payload) {
        try {
            log.debug("상태 업데이트 수신: {}", payload);

            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            String deviceId = (String) data.get(KEY_DEVICE_ID);
            Map<String, Object> status = (Map<String, Object>) data.get(KEY_STATUS);

            log.info("액추에이터 상태 업데이트: deviceId={}, status={}", deviceId, status);

        } catch (Exception e) {
            log.error("상태 업데이트 처리 중 오류: {}", e.getMessage(), e);
        }
    }

    private Float getFloatValue(Object value) {
        if (value instanceof Number number) {
            return number.floatValue();
        } else if (value instanceof String str) {
            try {
                return Float.parseFloat(str);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
