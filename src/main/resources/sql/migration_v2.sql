-- =======================================================
-- PlanBridge Migration v2
-- 적용 방법: Oracle SQL*Plus 또는 DBeaver에서 실행
-- =======================================================

-- 1. 변경 요청 테이블: COMPONENT_ID를 NULL 허용으로 변경
--    (Chrome Extension 없이 위저드로 변경 요청 생성 가능하도록)
ALTER TABLE PB_CHANGE_REQUEST MODIFY COMPONENT_ID VARCHAR2(36) NULL;

-- 2. 변경 요청 테이블: 자유 텍스트 컴포넌트 설명 컬럼 추가
ALTER TABLE PB_CHANGE_REQUEST ADD COMPONENT_DESCRIPTION VARCHAR2(500) NULL;

COMMIT;
