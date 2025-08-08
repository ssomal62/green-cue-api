package com.greencue.domain.sensor.repository;

import com.greencue.domain.sensor.model.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

    // 센서 타입별 최신 데이터 조회
    List<SensorData> findTop10ByTypeOrderByCreatedAtDesc(String type);

    // 특정 기간 데이터 조회
    @Query("SELECT s FROM SensorData s WHERE s.type = :type AND s.createdAt BETWEEN :startTime AND :endTime ORDER BY s.createdAt DESC")
    List<SensorData> findByTypeAndTimeRange(@Param("type") String type,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    // 최신 센서 데이터 조회 (모든 타입)
    @Query("SELECT s FROM SensorData s WHERE s.id IN (SELECT MAX(s2.id) FROM SensorData s2 GROUP BY s2.type)")
    List<SensorData> findLatestDataByType();
}
