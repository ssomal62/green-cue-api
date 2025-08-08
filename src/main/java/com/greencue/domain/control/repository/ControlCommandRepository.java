package com.greencue.domain.control.repository;

import com.greencue.domain.control.model.ControlCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ControlCommandRepository extends JpaRepository<ControlCommand, Long> {

    // 타겟별 최신 명령 조회
    List<ControlCommand> findTop10ByTargetOrderByCreatedAtDesc(String target);

    // 규칙에 의한 명령 조회
    List<ControlCommand> findByRuleIdOrderByCreatedAtDesc(Long ruleId);
}
