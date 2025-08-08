package com.greencue.domain.sensor.api;

import com.greencue.domain.sensor.api.dto.SensorDataRequest;
import com.greencue.domain.sensor.api.dto.SensorDataResponse;
import com.greencue.domain.sensor.service.SensorDataService;
import com.greencue.shared.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sensor")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SensorDataController {

    private final SensorDataService sensorDataService;

    // 센서 데이터 저장
    @PostMapping("/data")
    public ResponseEntity<ApiResponse<SensorDataResponse>> saveSensorData(@RequestBody SensorDataRequest sensorData) {
        SensorDataResponse saved = sensorDataService.saveSensorData(sensorData);
        return ResponseEntity.ok(ApiResponse.success(saved, "센서 데이터가 성공적으로 저장되었습니다."));
    }

    // 모든 센서의 최신 데이터 조회
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<List<SensorDataResponse>>> getLatestData() {
        List<SensorDataResponse> latestData = sensorDataService.getLatestDataByType();
        return ResponseEntity.ok(ApiResponse.success(latestData, "모든 센서의 최신 데이터를 조회했습니다."));
    }

    // 특정 센서 타입의 최신 데이터 조회
    @GetMapping("/latest/{type}")
    public ResponseEntity<ApiResponse<SensorDataResponse>> getLatestDataByType(@PathVariable String type) {
        SensorDataResponse latestData = sensorDataService.getLatestFromCache(type);
        if (latestData == null) {
            throw new com.greencue.domain.sensor.exception.InvalidSensorDataException("해당 센서 타입의 데이터를 찾을 수 없습니다: " + type);
        }
        return ResponseEntity.ok(ApiResponse.success(latestData, "센서 데이터를 조회했습니다."));
    }

    // 특정 센서 타입의 최근 데이터 조회
    @GetMapping("/{type}/recent")
    public ResponseEntity<ApiResponse<List<SensorDataResponse>>> getRecentDataByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "10") int limit) {
        List<SensorDataResponse> recentData = sensorDataService.getRecentDataByType(type, limit);
        return ResponseEntity.ok(ApiResponse.success(recentData, "최근 센서 데이터를 조회했습니다."));
    }

    // 특정 기간 데이터 조회
    @GetMapping("/{type}/range")
    public ResponseEntity<ApiResponse<List<SensorDataResponse>>> getDataByTimeRange(
            @PathVariable String type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<SensorDataResponse> data = sensorDataService.getDataByTimeRange(type, startTime, endTime);
        return ResponseEntity.ok(ApiResponse.success(data, "기간별 센서 데이터를 조회했습니다."));
    }

    // 센서 타입 목록 조회
    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<String>>> getSensorTypes() {
        List<String> types = List.of("light", "temp", "humi");
        return ResponseEntity.ok(ApiResponse.success(types, "센서 타입 목록을 조회했습니다."));
    }
}
