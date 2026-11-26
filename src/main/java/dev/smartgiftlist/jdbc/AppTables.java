package dev.smartgiftlist.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Standalone schema and table initializer matching docker/init.sql.
 */
public class AppTables {

    Connection jdbcConn;
    private final String schema;

    public AppTables() {
        this.schema = PostgreSQLAccess.validateSchemaIdentifier(
                PostgreSQLAccess.resolveSetting("DB_SCHEMA", "smartgiftlist")
        );
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        AppTables app = new AppTables();
        app.jdbcConn = new PostgreSQLAccess().getConnection();
        app.initTables();
    }

    public void initTables() throws ClassNotFoundException, SQLException {
        this.createSchemaIfNeeded();
        this.dropTablesCascade();
        this.createTableUsers();
        this.createTableWishlists();
        this.createTableGifts();
        this.createTableUserGiftConnections();
        this.createTableUserWishlistConnections();
        this.createTableWishlistComments();
    }

    public void createSchemaIfNeeded() throws SQLException {
        String sql = "CREATE SCHEMA IF NOT EXISTS " + schema;
        executeSql(sql);
        executeSql("SET search_path TO " + schema);
        System.out.println("[DB] Schema '" + schema + "' ensured");
    }

    public void dropTablesCascade() throws SQLException {
        dropTable("WishlistComments");
        dropTable("UserGiftConnections");
        dropTable("UserWishlistConnections");
        dropTable("Gifts");
        dropTable("Wishlists");
        dropTable("Users");
        System.out.println("[DB] Existing tables dropped");
    }

    public void dropTable(String tableName) throws SQLException {
        executeSql("DROP TABLE IF EXISTS " + tableName + " CASCADE");
    }

    public void createTableUsers() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS Users ("
                + "UserID SERIAL PRIMARY KEY, "
                + "Username VARCHAR(200) UNIQUE, "
                + "Email VARCHAR(200) UNIQUE, "
                + "Password VARCHAR(200), "
                + "Birthday DATE, "
                + "IsAdmin BOOLEAN DEFAULT FALSE, "
                + "Icon VARCHAR(200), "
                + "JoinDate TIMESTAMP"
                + ")";
        executeSql(sql);
        System.out.println("[DB] Table Users created");
    }

    public void createTableWishlists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS Wishlists ("
                + "WishlistID SERIAL PRIMARY KEY, "
                + "NetID VARCHAR(200) UNIQUE, "
                + "Title VARCHAR(200), "
                + "Event VARCHAR(200), "
                + "EventDate DATE, "
                + "Description TEXT, "
                + "URL VARCHAR(200), "
                + "Icon VARCHAR(200), "
                + "UserID INT, "
                + "CreateDate TIMESTAMP, "
                + "FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE"
                + ")";
        executeSql(sql);
        System.out.println("[DB] Table Wishlists created");
    }

    public void createTableGifts() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS Gifts ("
                + "GiftID SERIAL PRIMARY KEY, "
                + "WishlistID INT, "
                + "Title VARCHAR(200), "
                + "NumberOfPeople INT, "
                + "Surprise BOOLEAN, "
                + "Description TEXT, "
                + "Price DECIMAL, "
                + "Url VARCHAR(200), "
                + "Icon VARCHAR(200), "
                + "CreateDate TIMESTAMP, "
                + "CreatorUserID INT, "
                + "FOREIGN KEY (WishlistID) REFERENCES Wishlists(WishlistID) ON DELETE CASCADE"
                + ")";
        executeSql(sql);
        System.out.println("[DB] Table Gifts created");
    }

    public void createTableUserGiftConnections() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS UserGiftConnections ("
                + "UserGiftConnectionID SERIAL PRIMARY KEY, "
                + "UserID INT, "
                + "GiftID INT, "
                + "LinkDate TIMESTAMP, "
                + "FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE, "
                + "FOREIGN KEY (GiftID) REFERENCES Gifts(GiftID) ON DELETE CASCADE"
                + ")";
        executeSql(sql);
        System.out.println("[DB] Table UserGiftConnections created");
    }

    public void createTableUserWishlistConnections() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS UserWishlistConnections ("
                + "UserWishlistConnectionID SERIAL PRIMARY KEY, "
                + "UserID INT, "
                + "WishlistID INT, "
                + "IsOwner BOOLEAN DEFAULT FALSE, "
                + "LinkDate TIMESTAMP, "
                + "FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE, "
                + "FOREIGN KEY (WishlistID) REFERENCES Wishlists(WishlistID) ON DELETE CASCADE"
                + ")";
        executeSql(sql);
        System.out.println("[DB] Table UserWishlistConnections created");
    }

    public void createTableWishlistComments() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS WishlistComments ("
                + "CommentID SERIAL PRIMARY KEY, "
                + "WishlistID INT, "
                + "UserID INT, "
                + "CommentText TEXT, "
                + "CommentType INT CHECK (CommentType IN (1, 2)), "
                + "CommentTime TIMESTAMP, "
                + "FOREIGN KEY (WishlistID) REFERENCES Wishlists(WishlistID) ON DELETE CASCADE, "
                + "FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE"
                + ")";
        executeSql(sql);
        System.out.println("[DB] Table WishlistComments created");
    }

    private void executeSql(String sql) throws SQLException {
        System.out.println("[DB] " + sql);
        try (Statement stmt = this.jdbcConn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }
}
