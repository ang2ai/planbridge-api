# PlanBridge API (Spring Boot 백엔드)

## 역할
- 정책/컴포넌트 CRUD (Oracle DB)
- 변경 요청 관리
- AI 분석 워커 작업 큐 관리
- 크롬 익스텐션 → 스캔 데이터 수신

## 기술 스택
- Spring Boot 3.x + Java 17
- JPA + Oracle DB
- Spring Async (비동기 처리)
- SSE (실시간 알림)

## 환경변수
- DB_URL: Oracle JDBC URL
- DB_USERNAME / DB_PASSWORD
- REPOS_BASE_PATH: Git Mirror 소스 경로 (/repos)
- WORKER_QUEUE_TABLE: PB_ANALYSIS_QUEUE

## 실행
```bash
./mvnw spring-boot:run
# 또는
./gradlew bootRun
```

## 설계 문서 (우선순위 순)
@docs/planbridge-final-architecture.md
@docs/planbridge-mapping-design.md
@docs/planbridge-policy-management-design.md
@docs/planbridge-usecases.md

## 문서 우선순위 규칙
- 전체 아키텍처/방향 → planbridge-final-architecture.md 우선
- DB 스키마/매핑 → planbridge-mapping-design.md 우선
- 정책 관련 → planbridge-policy-management-design.md 우선
- 문서 간 충돌 시 → planbridge-final-architecture.md 최종 기준

## 개발 원칙
- DB 스키마는 docs/planbridge-mapping-design.md의 DDL 기준
- API 엔드포인트는 docs/planbridge-final-architecture.md 섹션 2.3 기준
- Oracle MERGE INTO 사용 (upsert)
- 소프트 삭제 (STATUS = 'DELETED')
