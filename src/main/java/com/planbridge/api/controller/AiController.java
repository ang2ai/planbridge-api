package com.planbridge.api.controller;

import com.planbridge.api.dto.response.ApiResponse;
import com.planbridge.api.service.AiPolicyService;
import com.planbridge.api.service.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final SseEmitterRegistry sseEmitterRegistry;
    private final AiPolicyService aiPolicyService;

    private static final Map<String, List<String>> POLICY_KEYWORDS = Map.of(
        "BUSINESS_RULE", List.of("규칙", "조건", "예외", "허용", "금지", "필수", "비회원", "회원", "권한", "업무"),
        "UI_SPEC", List.of("색상", "크기", "레이아웃", "디자인", "스타일", "여백", "화면", "모양"),
        "INTERACTION", List.of("클릭", "호버", "스크롤", "애니메이션", "전환", "이동", "열기", "닫기", "토스트"),
        "VALIDATION", List.of("검증", "유효", "형식", "범위", "최소", "최대", "이메일", "전화", "오류"),
        "TEXT_CONTENT", List.of("문구", "텍스트", "버튼명", "레이블", "메시지", "안내", "설명"),
        "API_SPEC", List.of("api", "데이터", "서버", "요청", "응답", "연동", "엔드포인트", "저장", "조회")
    );

    private static final Map<String, String> POLICY_REASONS = Map.of(
        "BUSINESS_RULE", "업무 조건이나 허용/금지 규칙을 설명하는 내용입니다.",
        "UI_SPEC", "화면의 시각적 요소나 레이아웃을 다루는 내용입니다.",
        "INTERACTION", "사용자 동작이나 애니메이션 등 상호작용을 다루는 내용입니다.",
        "VALIDATION", "입력값 검증이나 유효성 조건을 다루는 내용입니다.",
        "TEXT_CONTENT", "화면에 표시되는 문구나 메시지를 다루는 내용입니다.",
        "API_SPEC", "서버와의 데이터 연동을 다루는 내용입니다."
    );

    // -----------------------------------------------------------------------
    // POST /api/ai/structurize-policy
    // -----------------------------------------------------------------------

    @PostMapping("/structurize-policy")
    public ResponseEntity<ApiResponse<Map<String, Object>>> structurizePolicy(
            @RequestBody Map<String, String> body) {
        String policyId = body.get("policyId");
        String content = body.get("content");
        if (policyId == null || policyId.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("policyId는 필수입니다."));
        }
        Map<String, Object> schema = aiPolicyService.structurizePolicy(policyId, content);
        return ResponseEntity.ok(ApiResponse.ok("정책 스키마가 생성되었습니다", schema));
    }

    @PostMapping("/recommend-policy-type")
    public ResponseEntity<ApiResponse<Map<String, Object>>> recommendPolicyType(
            @RequestBody Map<String, String> body) {

        String description = body.getOrDefault("description", "").toLowerCase();

        // 키워드 매칭으로 점수 계산
        Map<String, Integer> scores = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : POLICY_KEYWORDS.entrySet()) {
            int score = (int) entry.getValue().stream()
                    .filter(description::contains)
                    .count();
            scores.put(entry.getKey(), score);
        }

        // 최고 점수 유형 선택
        String bestType = scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("BUSINESS_RULE");

        int bestScore = scores.getOrDefault(bestType, 0);
        int total = scores.values().stream().mapToInt(Integer::intValue).sum();
        double confidence = total > 0 ? Math.min(0.95, (double) bestScore / total * 1.5 + 0.3) : 0.4;

        // 대안 유형 (상위 2개)
        List<Map<String, String>> alternatives = scores.entrySet().stream()
                .filter(e -> !e.getKey().equals(bestType) && e.getValue() > 0)
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(2)
                .map(e -> Map.of("type", e.getKey(), "reason", POLICY_REASONS.getOrDefault(e.getKey(), "")))
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("recommendedType", bestType);
        result.put("confidence", Math.round(confidence * 100.0) / 100.0);
        result.put("reason", POLICY_REASONS.getOrDefault(bestType, ""));
        result.put("alternatives", alternatives);

        log.info("Policy type recommendation: {} (confidence: {})", bestType, confidence);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/completed")
    public ResponseEntity<ApiResponse<Map<String, String>>> analysisCompleted(
            @RequestBody Map<String, Object> body) {
        String queueId = (String) body.get("queueId");
        String analysisType = (String) body.get("analysisType");
        String requestId = (String) body.get("requestId");
        log.info("Analysis completed notification: queueId={}, type={}, requestId={}", queueId, analysisType, requestId);

        // SSE emitter에 완료 알림 전달
        // requestId가 있으면 requestId 기준, 없으면 queueId 기준으로 SSE 전달 시도
        String targetId = (requestId != null) ? requestId : queueId;
        if (targetId != null) {
            Map<String, Object> notifyData = new LinkedHashMap<>();
            notifyData.put("queueId", queueId);
            notifyData.put("requestId", requestId);
            notifyData.put("analysisType", analysisType);
            notifyData.put("status", "COMPLETED");
            sseEmitterRegistry.notifyCompleted(targetId, notifyData);
        }

        return ResponseEntity.ok(ApiResponse.ok(Map.of("status", "received")));
    }
}
