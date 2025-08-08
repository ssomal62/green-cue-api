package com.greencue.domain.automation.api;

import com.greencue.domain.automation.api.dto.AutomationRuleRequest;
import com.greencue.domain.automation.api.dto.AutomationRuleResponse;
import com.greencue.domain.automation.service.AutomationRuleService;
import com.greencue.shared.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/automation")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AutomationController {

    private final AutomationRuleService automationRuleService;

    // 자동화 규칙 생성
    @PostMapping("/rule")
    public ResponseEntity<ApiResponse<AutomationRuleResponse>> createRule(@RequestBody AutomationRuleRequest rule) {
        AutomationRuleResponse created = automationRuleService.createRule(rule);
        return ResponseEntity.ok(ApiResponse.success(created, "자동화 규칙이 성공적으로 생성되었습니다."));
    }

    // 자연어 명령을 규칙으로 변환
    @PostMapping("/natural-language")
    public ResponseEntity<ApiResponse<AutomationRuleResponse>> createRuleFromNaturalLanguage(@RequestBody String naturalCommand) {
        AutomationRuleResponse created = automationRuleService.createRuleFromNaturalLanguage(naturalCommand);
        return ResponseEntity.ok(ApiResponse.success(created, "자연어 명령이 규칙으로 변환되었습니다."));
    }

    // 활성화된 규칙 조회
    @GetMapping("/rules/active")
    public ResponseEntity<ApiResponse<List<AutomationRuleResponse>>> getActiveRules() {
        List<AutomationRuleResponse> rules = automationRuleService.getActiveRules();
        return ResponseEntity.ok(ApiResponse.success(rules, "활성화된 규칙 목록을 조회했습니다."));
    }

    // 규칙 활성화/비활성화
    @PutMapping("/rule/{ruleId}/toggle")
    public ResponseEntity<ApiResponse<AutomationRuleResponse>> toggleRule(
            @PathVariable Long ruleId,
            @RequestParam boolean isActive) {
        AutomationRuleResponse updated = automationRuleService.toggleRule(ruleId, isActive);
        String message = isActive ? "규칙이 활성화되었습니다." : "규칙이 비활성화되었습니다.";
        return ResponseEntity.ok(ApiResponse.success(updated, message));
    }

    // 자연어 명령 예시 조회
    @GetMapping("/examples")
    public ResponseEntity<ApiResponse<List<String>>> getNaturalLanguageExamples() {
        List<String> examples = List.of(
            "조도가 200 lux 이하일 때 LED 켜줘",
            "온도가 30도 이상이면 팬 켜줘",
            "조도가 400 lux 이상이면 LED 꺼줘",
            "습도가 40% 이하로 떨어지면 알림 보내줘"
        );
        return ResponseEntity.ok(ApiResponse.success(examples, "자연어 명령 예시를 조회했습니다."));
    }
}
