package com.planbridge.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planbridge.api.entity.PbPolicy;
import com.planbridge.api.exception.ResourceNotFoundException;
import com.planbridge.api.repository.PbPolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AiPolicyService {

    private final PbPolicyRepository policyRepository;
    private final ObjectMapper objectMapper;

    @Value("${planbridge.worker.url:http://localhost:3002}")
    private String workerUrl;

    // -------------------------------------------------------------------------
    // 1. Structurize policy via AI worker
    // -------------------------------------------------------------------------

    @Transactional
    public Map<String, Object> structurizePolicy(String policyId, String content) {
        PbPolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", policyId));

        // Call planbridge-worker analyze endpoint
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("policyId", policyId);
        requestBody.put("content", content != null ? content : policy.getPolicyContent());
        requestBody.put("policyType", policy.getPolicyType());
        requestBody.put("policyTitle", policy.getPolicyTitle());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        String analyzeUrl = workerUrl + "/api/analyze/policy-schema";
        log.info("Calling worker structurize: {}", analyzeUrl);

        Map<String, Object> schema;
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(analyzeUrl, request, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object data = response.getBody().get("data");
                if (data instanceof Map) {
                    //noinspection unchecked
                    schema = (Map<String, Object>) data;
                } else {
                    schema = response.getBody();
                }
            } else {
                schema = fallbackSchema(policy);
            }
        } catch (Exception e) {
            log.warn("Worker call failed ({}), using fallback schema: {}", analyzeUrl, e.getMessage());
            schema = fallbackSchema(policy);
        }

        // Persist the returned schema into the policy
        try {
            String schemaJson = objectMapper.writeValueAsString(schema);
            policy.setPolicySchema(schemaJson);
            policyRepository.save(policy);
        } catch (Exception e) {
            log.warn("Failed to persist policy schema: {}", e.getMessage());
        }

        return schema;
    }

    private Map<String, Object> fallbackSchema(PbPolicy policy) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("policyId", policy.getPolicyId());
        schema.put("policyTitle", policy.getPolicyTitle());
        schema.put("policyType", policy.getPolicyType());
        schema.put("scope", policy.getScope());
        schema.put("properties", new LinkedHashMap<>());
        schema.put("required", new ArrayList<>());
        schema.put("generated", "fallback");
        return schema;
    }

    // -------------------------------------------------------------------------
    // 2. Convert policy to Claude Code development prompt
    // -------------------------------------------------------------------------

    public String toDevelopmentPrompt(String policyId) {
        PbPolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", policyId));

        String schemaBlock = "";
        if (policy.getPolicySchema() != null && !policy.getPolicySchema().isBlank()) {
            schemaBlock = "\n## JSON 스키마\n```json\n" + policy.getPolicySchema() + "\n```\n";
        }

        return "# Claude Code 개발 프롬프트\n\n"
                + "## 정책: " + nvl(policy.getPolicyTitle()) + "\n"
                + "**유형**: " + nvl(policy.getPolicyType()) + "\n"
                + "**범위**: " + nvl(policy.getScope()) + "\n"
                + "\n## 정책 내용\n" + nvl(policy.getPolicyContent()) + "\n"
                + schemaBlock
                + "\n## 개발 요청사항\n"
                + "위 정책을 코드에 반영해주세요. 다음 사항을 구현하세요:\n"
                + "- 정책에 명시된 모든 검증 규칙 적용\n"
                + "- TypeScript/Zod 타입 정의 생성\n"
                + "- 관련 컴포넌트에 정책 로직 적용\n";
    }

    // -------------------------------------------------------------------------
    // 3. Convert policy JSON schema to TypeScript/Zod validation code
    // -------------------------------------------------------------------------

    public String toZodCode(String policyId) {
        PbPolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", policyId));

        String camelName = toCamelCase(policy.getPolicyTitle());
        String pascalName = toPascalCase(policy.getPolicyTitle());

        StringBuilder sb = new StringBuilder();
        sb.append("import { z } from 'zod';\n\n");
        sb.append("// ").append(nvl(policy.getPolicyTitle())).append(" 검증 스키마\n");
        sb.append("export const ").append(camelName).append("Schema = z.object({\n");

        if (policy.getPolicySchema() != null && !policy.getPolicySchema().isBlank()) {
            try {
                Map<String, Object> schemaMap = objectMapper.readValue(
                        policy.getPolicySchema(), new TypeReference<Map<String, Object>>() {});
                Object propertiesObj = schemaMap.get("properties");
                Object requiredObj = schemaMap.get("required");

                Set<String> required = new LinkedHashSet<>();
                if (requiredObj instanceof List) {
                    //noinspection unchecked
                    ((List<Object>) requiredObj).forEach(r -> required.add(r.toString()));
                }

                if (propertiesObj instanceof Map) {
                    //noinspection unchecked
                    Map<String, Object> properties = (Map<String, Object>) propertiesObj;
                    for (Map.Entry<String, Object> entry : properties.entrySet()) {
                        String fieldName = entry.getKey();
                        String zodType = resolveZodType(entry.getValue());
                        boolean isRequired = required.contains(fieldName);
                        sb.append("  ").append(fieldName).append(": ").append(zodType);
                        if (!isRequired) sb.append(".optional()");
                        sb.append(",\n");
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse policySchema JSON for policyId={}: {}", policyId, e.getMessage());
                sb.append("  // 스키마 파싱 실패 — 수동으로 필드를 추가하세요\n");
            }
        } else {
            sb.append("  // 스키마가 없습니다 — 필드를 추가하세요\n");
        }

        sb.append("});\n\n");
        sb.append("export type ").append(pascalName).append(" = z.infer<typeof ")
          .append(camelName).append("Schema>;\n");

        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String resolveZodType(Object fieldDef) {
        if (!(fieldDef instanceof Map)) return "z.unknown()";
        //noinspection unchecked
        Map<String, Object> def = (Map<String, Object>) fieldDef;
        String type = String.valueOf(def.getOrDefault("type", ""));
        String format = String.valueOf(def.getOrDefault("format", ""));

        switch (type) {
            case "string":
                if ("email".equalsIgnoreCase(format)) return "z.string().email()";
                if ("uri".equalsIgnoreCase(format) || "url".equalsIgnoreCase(format)) return "z.string().url()";
                if ("date".equalsIgnoreCase(format)) return "z.string().regex(/^\\d{4}-\\d{2}-\\d{2}$/)";
                if ("date-time".equalsIgnoreCase(format)) return "z.string().datetime()";
                Object minLen = def.get("minLength");
                Object maxLen = def.get("maxLength");
                String strChain = "z.string()";
                if (minLen != null) strChain += ".min(" + minLen + ")";
                if (maxLen != null) strChain += ".max(" + maxLen + ")";
                return strChain;
            case "number":
            case "integer": {
                String numChain = "integer".equals(type) ? "z.number().int()" : "z.number()";
                Object min = def.get("minimum");
                Object max = def.get("maximum");
                if (min != null) numChain += ".min(" + min + ")";
                if (max != null) numChain += ".max(" + max + ")";
                return numChain;
            }
            case "boolean":
                return "z.boolean()";
            case "array":
                return "z.array(z.unknown())";
            case "object":
                return "z.record(z.unknown())";
            default:
                return "z.unknown()";
        }
    }

    private static String toCamelCase(String title) {
        if (title == null || title.isBlank()) return "policy";
        String[] words = title.trim().replaceAll("[^a-zA-Z0-9가-힣 ]", "").split("\\s+");
        if (words.length == 0) return "policy";
        StringBuilder sb = new StringBuilder(words[0].toLowerCase());
        for (int i = 1; i < words.length; i++) {
            String w = words[i];
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)));
                sb.append(w.substring(1).toLowerCase());
            }
        }
        String result = sb.toString();
        // If starts with digit, prefix with underscore
        return result.isEmpty() ? "policy" : (Character.isDigit(result.charAt(0)) ? "_" + result : result);
    }

    private static String toPascalCase(String title) {
        String camel = toCamelCase(title);
        if (camel.isEmpty()) return "Policy";
        return Character.toUpperCase(camel.charAt(0)) + camel.substring(1);
    }

    private static String nvl(String value) {
        return value != null ? value : "";
    }
}
