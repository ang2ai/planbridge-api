package com.planbridge.api.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        createTableIfNotExists();
        createValidationRuleTableIfNotExists();
    }

    private void createTableIfNotExists() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = 'PB_COMPONENT_TEMPLATE'",
            Integer.class
        );

        if (count == null || count == 0) {
            log.info("Creating PB_COMPONENT_TEMPLATE table...");
            jdbcTemplate.execute(
                "CREATE TABLE PB_COMPONENT_TEMPLATE (" +
                "TEMPLATE_ID VARCHAR2(36) DEFAULT SYS_GUID() PRIMARY KEY," +
                "PROJECT_ID VARCHAR2(36) NOT NULL REFERENCES PB_PROJECT(PROJECT_ID)," +
                "TEMPLATE_NAME VARCHAR2(200) NOT NULL," +
                "COMPONENT_TYPE VARCHAR2(30) NOT NULL," +
                "DESCRIPTION VARCHAR2(1000)," +
                "TEMPLATE_JSON CLOB," +
                "POLICY_TAGS VARCHAR2(500)," +
                "USAGE_COUNT NUMBER(7) DEFAULT 0," +
                "STATUS VARCHAR2(20) DEFAULT 'ACTIVE'," +
                "CREATED_BY VARCHAR2(100) NOT NULL," +
                "CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP," +
                "UPDATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP" +
                ")"
            );
            jdbcTemplate.execute("CREATE INDEX IDX_PB_CT_PROJECT ON PB_COMPONENT_TEMPLATE(PROJECT_ID)");
            jdbcTemplate.execute("CREATE INDEX IDX_PB_CT_STATUS ON PB_COMPONENT_TEMPLATE(STATUS)");
            log.info("PB_COMPONENT_TEMPLATE table created successfully");
        } else {
            log.debug("PB_COMPONENT_TEMPLATE table already exists");
        }
    }

    private void createValidationRuleTableIfNotExists() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = 'PB_VALIDATION_RULE'",
            Integer.class
        );
        if (count == null || count == 0) {
            log.info("Creating PB_VALIDATION_RULE table...");
            jdbcTemplate.execute(
                "CREATE TABLE PB_VALIDATION_RULE (" +
                "RULE_ID VARCHAR2(36) DEFAULT SYS_GUID() PRIMARY KEY," +
                "POLICY_ID VARCHAR2(100) NOT NULL REFERENCES PB_POLICY(POLICY_ID)," +
                "RULE_TYPE VARCHAR2(50) NOT NULL," +
                "FIELD_NAME VARCHAR2(200)," +
                "RULE_VALUE VARCHAR2(500)," +
                "ERROR_MESSAGE VARCHAR2(500)," +
                "SORT_ORDER NUMBER(5) DEFAULT 0," +
                "CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP)"
            );
            jdbcTemplate.execute("CREATE INDEX IDX_PB_VR_POLICY ON PB_VALIDATION_RULE(POLICY_ID)");
            log.info("PB_VALIDATION_RULE table created successfully");
        } else {
            log.debug("PB_VALIDATION_RULE table already exists");
        }
    }
}
