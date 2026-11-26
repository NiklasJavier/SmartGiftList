package dev.smartgiftlist.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Non-destructive PostgreSQL schema, constraint, and index initializer matching docker/init.sql.
 */
public final class SchemaInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaInitializer.class);

    private static final List<String> DDL_STATEMENTS = List.of(
            """
            CREATE TABLE IF NOT EXISTS Users (
                UserID SERIAL PRIMARY KEY,
                Username VARCHAR(200) UNIQUE NOT NULL,
                Email VARCHAR(200) UNIQUE NOT NULL,
                Password VARCHAR(200) NOT NULL,
                Birthday DATE,
                IsAdmin BOOLEAN DEFAULT FALSE,
                Icon VARCHAR(200),
                JoinDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS Wishlists (
                WishlistID SERIAL PRIMARY KEY,
                NetID VARCHAR(200) UNIQUE NOT NULL,
                Title VARCHAR(200) NOT NULL,
                Event VARCHAR(200),
                EventDate DATE,
                Description TEXT,
                URL VARCHAR(500),
                Icon VARCHAR(200),
                UserID INT NOT NULL,
                CreateDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS Gifts (
                GiftID SERIAL PRIMARY KEY,
                WishlistID INT NOT NULL,
                Title VARCHAR(200) NOT NULL,
                NumberOfPeople INT NOT NULL DEFAULT 1,
                Surprise BOOLEAN NOT NULL DEFAULT FALSE,
                Description TEXT,
                Price DECIMAL(10, 2),
                Url VARCHAR(500),
                Icon VARCHAR(200),
                CreateDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                CreatorUserID INT,
                FOREIGN KEY (WishlistID) REFERENCES Wishlists(WishlistID) ON DELETE CASCADE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS UserGiftConnections (
                UserGiftConnectionID SERIAL PRIMARY KEY,
                UserID INT NOT NULL,
                GiftID INT NOT NULL,
                LinkDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE (UserID, GiftID),
                FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE,
                FOREIGN KEY (GiftID) REFERENCES Gifts(GiftID) ON DELETE CASCADE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS UserWishlistConnections (
                UserWishlistConnectionID SERIAL PRIMARY KEY,
                UserID INT NOT NULL,
                WishlistID INT NOT NULL,
                IsOwner BOOLEAN DEFAULT FALSE,
                LinkDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE (UserID, WishlistID),
                FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE,
                FOREIGN KEY (WishlistID) REFERENCES Wishlists(WishlistID) ON DELETE CASCADE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS WishlistComments (
                CommentID SERIAL PRIMARY KEY,
                WishlistID INT NOT NULL,
                UserID INT,
                CommentText TEXT NOT NULL,
                CommentType INT NOT NULL CHECK (CommentType IN (1, 2)),
                CommentTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (WishlistID) REFERENCES Wishlists(WishlistID) ON DELETE CASCADE,
                FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE
            )
            """,
            "CREATE INDEX IF NOT EXISTS idx_wishlists_user ON Wishlists(UserID)",
            "CREATE INDEX IF NOT EXISTS idx_gifts_wishlist ON Gifts(WishlistID)",
            "CREATE INDEX IF NOT EXISTS idx_uwc_user ON UserWishlistConnections(UserID)",
            "CREATE INDEX IF NOT EXISTS idx_ugc_gift ON UserGiftConnections(GiftID)",
            "CREATE INDEX IF NOT EXISTS idx_comments_wishlist_time ON WishlistComments(WishlistID, CommentTime DESC)"
    );

    private SchemaInitializer() {
    }

    public static void main(String[] args) {
        DatabaseConfig config = DatabaseConfig.fromEnvironment();
        try (HikariDataSource ds = DataSourceFactory.create(config)) {
            ensureSchema(ds, config.getSchema());
        }
    }

    public static void ensureSchema(DataSource dataSource, String schema) {
        String safeSchema = DatabaseConfig.validateSchemaIdentifier(schema);
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE SCHEMA IF NOT EXISTS " + safeSchema);
            stmt.execute("SET search_path TO " + safeSchema);
            for (String ddl : DDL_STATEMENTS) {
                stmt.execute(ddl);
            }
            LOG.info("Ensured database schema '{}', tables, constraints, and indexes", safeSchema);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to initialize schema '" + safeSchema + "'", ex);
        }
    }
}
