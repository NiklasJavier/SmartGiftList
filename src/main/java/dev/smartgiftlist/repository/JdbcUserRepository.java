package dev.smartgiftlist.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import javax.sql.DataSource;

import dev.smartgiftlist.model.IconCatalog;
import dev.smartgiftlist.model.User;

/**
 * Pooled JDBC implementation of {@link UserRepository}.
 */
public class JdbcUserRepository implements UserRepository {

    private static final String SELECT_USER_COLS =
            "SELECT UserID, Username, Email, Password, Birthday, IsAdmin, Icon, JoinDate FROM Users ";

    private final DataSource dataSource;

    public JdbcUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = SELECT_USER_COLS + "WHERE Username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapUser(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to load user by username", ex);
        }
    }

    @Override
    public Optional<User> findById(int userId) {
        String sql = SELECT_USER_COLS + "WHERE UserID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapUser(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to load user by ID", ex);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        return existsSingle("SELECT 1 FROM Users WHERE Username = ?", username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return existsSingle("SELECT 1 FROM Users WHERE Email = ?", email);
    }

    @Override
    public boolean existsByUsernameExcludingUser(String username, int excludedUserId) {
        String sql = "SELECT 1 FROM Users WHERE Username = ? AND UserID <> ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setInt(2, excludedUserId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to check duplicate username", ex);
        }
    }

    @Override
    public User create(String username, String email, String passwordHash, LocalDate birthday, String icon) {
        String safeIcon = IconCatalog.normalizeAvatarKey(icon);
        String sql = "INSERT INTO Users (Username, Email, Password, Birthday, IsAdmin, Icon, JoinDate) "
                + "VALUES (?, ?, ?, ?, FALSE, ?, CURRENT_TIMESTAMP) RETURNING UserID, JoinDate";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, passwordHash);
            if (birthday != null) {
                stmt.setDate(4, Date.valueOf(birthday));
            } else {
                stmt.setNull(4, Types.DATE);
            }
            stmt.setString(5, safeIcon);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                int id = rs.getInt("UserID");
                Timestamp joinTs = rs.getTimestamp("JoinDate");
                LocalDateTime joinDate = joinTs != null ? joinTs.toLocalDateTime() : LocalDateTime.now();
                return new User(id, username, email, passwordHash, birthday, false, safeIcon, joinDate);
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to create user", ex);
        }
    }

    @Override
    public boolean updateProfile(int userId, String username, String passwordHashOrNull, String icon) {
        String safeIcon = IconCatalog.normalizeAvatarKey(icon);
        boolean updatePassword = passwordHashOrNull != null && !passwordHashOrNull.isBlank();
        String sql = updatePassword
                ? "UPDATE Users SET Username = ?, Password = ?, Icon = ? WHERE UserID = ?"
                : "UPDATE Users SET Username = ?, Icon = ? WHERE UserID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            if (updatePassword) {
                stmt.setString(2, passwordHashOrNull);
                stmt.setString(3, safeIcon);
                stmt.setInt(4, userId);
            } else {
                stmt.setString(2, safeIcon);
                stmt.setInt(3, userId);
            }
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to update user profile", ex);
        }
    }

    @Override
    public boolean deleteById(int userId) {
        String sql = "DELETE FROM Users WHERE UserID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to delete user", ex);
        }
    }

    private boolean existsSingle(String sql, String value) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to run existence check", ex);
        }
    }

    private static User mapUser(ResultSet rs) throws SQLException {
        Date bdSql = rs.getDate("Birthday");
        Timestamp joinSql = rs.getTimestamp("JoinDate");
        return new User(
                rs.getInt("UserID"),
                rs.getString("Username"),
                rs.getString("Email"),
                rs.getString("Password"),
                bdSql != null ? bdSql.toLocalDate() : null,
                rs.getBoolean("IsAdmin"),
                rs.getString("Icon"),
                joinSql != null ? joinSql.toLocalDateTime() : null
        );
    }
}
