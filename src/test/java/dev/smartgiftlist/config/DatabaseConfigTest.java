package dev.smartgiftlist.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatabaseConfigTest {

    @AfterEach
    void clearProperties() {
        System.clearProperty("DB_URL");
        System.clearProperty("DB_USER");
        System.clearProperty("DB_PASSWORD");
        System.clearProperty("DB_SCHEMA");
    }

    @Test
    void usesSafeLocalDefaultsWhenUnconfigured() {
        DatabaseConfig config = DatabaseConfig.fromEnvironment();
        assertEquals("jdbc:postgresql://localhost:5432/smartgiftlist", config.getJdbcUrl());
        assertEquals("smartgiftlist", config.getUsername());
        assertEquals("smartgiftlist", config.getSchema());
    }

    @Test
    void readsConfiguredSystemProperties() {
        System.setProperty("DB_URL", "jdbc:postgresql://postgres:5432/customdb");
        System.setProperty("DB_USER", "custom_user");
        System.setProperty("DB_PASSWORD", "custom_pw");
        System.setProperty("DB_SCHEMA", "custom_schema");

        DatabaseConfig config = DatabaseConfig.fromEnvironment();
        assertEquals("jdbc:postgresql://postgres:5432/customdb", config.getJdbcUrl());
        assertEquals("custom_user", config.getUsername());
        assertEquals("custom_pw", config.getPassword());
        assertEquals("custom_schema", config.getSchema());
    }

    @Test
    void rejectsUnsafeSchemaIdentifiersToPreventSqlInjection() {
        assertEquals("smartgiftlist_v2", DatabaseConfig.validateSchemaIdentifier("smartgiftlist_v2"));
        assertThrows(IllegalArgumentException.class, () -> DatabaseConfig.validateSchemaIdentifier("smartgiftlist; DROP TABLE Users;--"));
        assertThrows(IllegalArgumentException.class, () -> DatabaseConfig.validateSchemaIdentifier("123bad"));
        assertThrows(IllegalArgumentException.class, () -> DatabaseConfig.validateSchemaIdentifier(""));
        assertThrows(IllegalArgumentException.class, () -> DatabaseConfig.validateSchemaIdentifier(null));
    }
}
