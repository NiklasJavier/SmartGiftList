package dev.smartgiftlist.jdbc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PostgreSQLAccessConfigTest {

    @AfterEach
    void clearProperties() {
        System.clearProperty("DB_URL");
        System.clearProperty("DB_USER");
        System.clearProperty("DB_PASSWORD");
        System.clearProperty("DB_SCHEMA");
    }

    @Test
    void usesSafeLocalDefaultsWhenUnconfigured() {
        PostgreSQLAccess config = PostgreSQLAccess.previewConfig();
        assertEquals("jdbc:postgresql://localhost:5432/smartgiftlist", config.getDbURL());
        assertEquals("smartgiftlist", config.getDbUser());
        assertEquals("smartgiftlist", config.getDbSchema());
    }

    @Test
    void readsConfiguredSystemProperties() {
        System.setProperty("DB_URL", "jdbc:postgresql://postgres:5432/customdb");
        System.setProperty("DB_USER", "custom_user");
        System.setProperty("DB_PASSWORD", "custom_pw");
        System.setProperty("DB_SCHEMA", "custom_schema");

        PostgreSQLAccess config = PostgreSQLAccess.previewConfig();
        assertEquals("jdbc:postgresql://postgres:5432/customdb", config.getDbURL());
        assertEquals("custom_user", config.getDbUser());
        assertEquals("custom_schema", config.getDbSchema());
    }

    @Test
    void rejectsUnsafeSchemaIdentifiersToPreventSqlInjection() {
        assertEquals("smartgiftlist_v2", PostgreSQLAccess.validateSchemaIdentifier("smartgiftlist_v2"));
        assertThrows(IllegalArgumentException.class, () -> PostgreSQLAccess.validateSchemaIdentifier("smartgiftlist; DROP TABLE Users;--"));
        assertThrows(IllegalArgumentException.class, () -> PostgreSQLAccess.validateSchemaIdentifier("123bad"));
        assertThrows(IllegalArgumentException.class, () -> PostgreSQLAccess.validateSchemaIdentifier(""));
        assertThrows(IllegalArgumentException.class, () -> PostgreSQLAccess.validateSchemaIdentifier(null));
    }
}
