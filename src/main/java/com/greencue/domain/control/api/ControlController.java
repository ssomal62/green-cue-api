package com.greencue.domain.control.api;

import com.greencue.domain.control.api.dto.ControlCommandRequest;
import com.greencue.domain.control.api.dto.ControlCommandResponse;
import com.greencue.domain.control.service.ControlCommandService;
import com.greencue.shared.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/control")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ControlController {

    private final ControlCommandService controlCommandService;

    // 제어 명령 실행
    @PostMapping("/command")
    public ResponseEntity<ApiResponse<ControlCommandResponse>> executeCommand(@RequestBody ControlCommandRequest command) {
        ControlCommandResponse executed = controlCommandService.executeCommand(command);
        return ResponseEntity.ok(ApiResponse.success(executed, "제어 명령이 성공적으로 실행되었습니다."));
    }

    // LED 밝기 조절
    @PostMapping("/led/brightness")
    public ResponseEntity<ApiResponse<ControlCommandResponse>> setLedBrightness(@RequestParam float brightness) {
        ControlCommandResponse command = controlCommandService.setLedBrightness(brightness);
        return ResponseEntity.ok(ApiResponse.success(command, "LED 밝기가 조절되었습니다."));
    }

    // LED ON/OFF
    @PostMapping("/led/power")
    public ResponseEntity<ApiResponse<ControlCommandResponse>> setLedPower(@RequestParam boolean isOn) {
        ControlCommandResponse command = controlCommandService.setLedPower(isOn);
        String message = isOn ? "LED가 켜졌습니다." : "LED가 꺼졌습니다.";
        return ResponseEntity.ok(ApiResponse.success(command, message));
    }

    // 팬 ON/OFF
    @PostMapping("/fan/power")
    public ResponseEntity<ApiResponse<ControlCommandResponse>> setFanPower(@RequestParam boolean isOn) {
        ControlCommandResponse command = controlCommandService.setFanPower(isOn);
        String message = isOn ? "팬이 켜졌습니다." : "팬이 꺼졌습니다.";
        return ResponseEntity.ok(ApiResponse.success(command, message));
    }

    // 타겟별 최근 명령 조회
    @GetMapping("/{target}/recent")
    public ResponseEntity<ApiResponse<List<ControlCommandResponse>>> getRecentCommandsByTarget(
            @PathVariable String target,
            @RequestParam(defaultValue = "10") int limit) {
        List<ControlCommandResponse> commands = controlCommandService.getRecentCommandsByTarget(target, limit);
        return ResponseEntity.ok(ApiResponse.success(commands, "최근 제어 명령을 조회했습니다."));
    }

    // 규칙에 의한 명령 조회
    @GetMapping("/rule/{ruleId}")
    public ResponseEntity<ApiResponse<List<ControlCommandResponse>>> getCommandsByRule(@PathVariable Long ruleId) {
        List<ControlCommandResponse> commands = controlCommandService.getCommandsByRule(ruleId);
        return ResponseEntity.ok(ApiResponse.success(commands, "규칙에 의한 제어 명령을 조회했습니다."));
    }

    // 제어 대상 목록 조회
    @GetMapping("/targets")
    public ResponseEntity<ApiResponse<List<String>>> getControlTargets() {
        List<String> targets = List.of("led", "fan");
        return ResponseEntity.ok(ApiResponse.success(targets, "제어 대상 목록을 조회했습니다."));
    }
}
