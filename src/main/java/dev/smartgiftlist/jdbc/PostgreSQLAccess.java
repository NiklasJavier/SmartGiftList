package dev.smartgiftlist.jdbc;

import java.util.regex.Pattern;

/**
 * Environment-configured PostgreSQL connection settings with safe local defaults.
 */
public class PostgreSQLAccess extends JDBCAccess {

    private static final Pattern VALID_SCHEMA = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    public PostgreSQLAccess() throws NoConnectionException {
        super();
    }

    protected PostgreSQLAccess(boolean connectImmediately) throws NoConnectionException {
        super(connectImmediately);
    }

    @Override
    public void setDBParms() {
        dbDrivername = "org.postgresql.Driver";
        dbURL        = resolveSetting("DB_URL", "jdbc:postgresql://localhost:5432/smartgiftlist");
        dbUser       = resolveSetting("DB_USER", "smartgiftlist");
        dbPassword   = resolveSetting("DB_PASSWORD", "smartgiftlist");
        dbSchema     = validateSchemaIdentifier(resolveSetting("DB_SCHEMA", "smartgiftlist"));
    }

    public static PostgreSQLAccess previewConfig() {
        try {
            return new PostgreSQLAccess(false);
        } catch (NoConnectionException ignored) {
            throw new IllegalStateException("Unexpected connection error during config preview", ignored);
        }
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

    public String getDbURL() {
        return dbURL;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbSchema() {
        return dbSchema;
    }
}
