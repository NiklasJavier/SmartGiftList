package dev.smartgiftlist.config;

import java.util.regex.Pattern;

/**
 * Immutable PostgreSQL configuration loaded from system properties or environment variables.
 */
public final class DatabaseConfig {

    private static final Pattern VALID_SCHEMA = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/smartgiftlist";
    private static final String DEFAULT_USER = "smartgiftlist";
    private static final String DEFAULT_PASSWORD = "smartgiftlist";
    private static final String DEFAULT_SCHEMA = "smartgiftlist";

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final String schema;

    private DatabaseConfig(String jdbcUrl, String username, String password, String schema) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.schema = validateSchemaIdentifier(schema);
    }

    public static DatabaseConfig fromEnvironment() {
        return new DatabaseConfig(
                resolveSetting("DB_URL", DEFAULT_URL),
                resolveSetting("DB_USER", DEFAULT_USER),
                resolveSetting("DB_PASSWORD", DEFAULT_PASSWORD),
                resolveSetting("DB_SCHEMA", DEFAULT_SCHEMA)
        );
    }

    public static DatabaseConfig of(String jdbcUrl, String username, String password, String schema) {
        return new DatabaseConfig(jdbcUrl, username, password, schema);
    }

    public static String resolveSetting(String key, String defaultValue) {
        String prop = System.getProperty(key);
        if (prop != null && !prop.isBlank()) {
            return prop.trim();
        }
        String env = System.getenv(key);
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        return defaultValue;
    }

    public static String validateSchemaIdentifier(String schema) {
        if (schema == null || !VALID_SCHEMA.matcher(schema).matches()) {
            throw new IllegalArgumentException("Invalid PostgreSQL schema identifier: " + schema);
        }
        return schema;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getSchema() {
        return schema;
    }
}
