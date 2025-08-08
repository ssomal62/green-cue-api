package com.greencue.domain.automation.repository;

import com.greencue.domain.automation.model.AutomationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutomationRuleRepository extends JpaRepository<AutomationRule, Long> {

    // 활성화된 규칙 조회
    List<AutomationRule> findByIsActiveTrue();

    // 조건에 따른 규칙 조회
    List<AutomationRule> findByConditionContainingAndIsActiveTrue(String condition);
}
