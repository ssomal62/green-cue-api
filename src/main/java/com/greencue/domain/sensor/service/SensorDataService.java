package com.greencue.domain.sensor.service;

import com.greencue.domain.sensor.api.dto.SensorDataRequest;
import com.greencue.domain.sensor.api.dto.SensorDataResponse;
import com.greencue.domain.sensor.model.SensorData;
import com.greencue.domain.sensor.repository.SensorDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class SensorDataService {

    private final SensorDataRepository sensorDataRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public SensorDataService(SensorDataRepository sensorDataRepository,
                            SimpMessagingTemplate messagingTemplate) {
        this.sensorDataRepository = sensorDataRepository;
        this.messagingTemplate = messagingTemplate;
        this.redisTemplate = null;
    }

    // Redis가 있을 때만 주입
    @Autowired(required = false)
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 센서 데이터 저장
    public SensorDataResponse saveSensorData(SensorDataRequest dto) {
        SensorData sensorData = dto.toEntity();
        SensorData saved = sensorDataRepository.save(sensorData);

        // Redis에 최신 데이터 캐시 (Redis가 활성화된 경우에만)
        if (redisTemplate != null) {
            String cacheKey = "sensor:latest:" + dto.type();
            redisTemplate.opsForValue().set(cacheKey, saved);
        }

        // WebSocket으로 실시간 전송
        messagingTemplate.convertAndSend("/topic/sensor-data", SensorDataResponse.fromEntity(saved));

        log.info("센서 데이터 저장: {} = {} {}", dto.type(), dto.sensorValue(), dto.unit());

        return SensorDataResponse.fromEntity(saved);
    }

    // 센서 타입별 최신 데이터 조회
    public List<SensorDataResponse> getLatestDataByType() {
        List<SensorData> latestData = sensorDataRepository.findLatestDataByType();
        return latestData.stream()
                .map(SensorDataResponse::fromEntity)
                .toList();
    }

    // 센서 타입별 최근 데이터 조회
    public List<SensorDataResponse> getRecentDataByType(String type, int limit) {
        List<SensorData> recentData = sensorDataRepository.findTop10ByTypeOrderByCreatedAtDesc(type);
        return recentData.stream()
                .limit(limit)
                .map(SensorDataResponse::fromEntity)
                .toList();
    }

    // 특정 기간 데이터 조회
    public List<SensorDataResponse> getDataByTimeRange(String type, LocalDateTime startTime, LocalDateTime endTime) {
        List<SensorData> data = sensorDataRepository.findByTypeAndTimeRange(type, startTime, endTime);
        return data.stream()
                .map(SensorDataResponse::fromEntity)
                .toList();
    }

    // Redis에서 최신 데이터 조회
    public SensorDataResponse getLatestFromCache(String type) {
        if (redisTemplate == null) {
            log.warn("Redis가 비활성화되어 있어 캐시에서 데이터를 조회할 수 없습니다.");
            return null;
        }

        String cacheKey = "sensor:latest:" + type;
        SensorData cached = (SensorData) redisTemplate.opsForValue().get(cacheKey);
        return cached != null ? SensorDataResponse.fromEntity(cached) : null;
    }
}
