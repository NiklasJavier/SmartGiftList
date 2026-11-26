package dev.smartgiftlist.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import dev.smartgiftlist.model.ChatComment;
import dev.smartgiftlist.model.CommentType;

/**
 * Pooled JDBC implementation of {@link CommentRepository}.
 */
public class JdbcCommentRepository implements CommentRepository {

    private final DataSource dataSource;

    public JdbcCommentRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<ChatComment> findByWishlistId(int wishlistId) {
        String sql = "SELECT wc.CommentID, wc.WishlistID, wc.UserID, wc.CommentText, wc.CommentType, wc.CommentTime, "
                + "u.Username, u.Icon "
                + "FROM WishlistComments wc "
                + "LEFT JOIN Users u ON wc.UserID = u.UserID "
                + "WHERE wc.WishlistID = ? "
                + "ORDER BY wc.CommentTime DESC, wc.CommentID DESC";
        List<ChatComment> comments = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, wishlistId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int rawUserId = rs.getInt("UserID");
                    Integer userId = rs.wasNull() ? null : rawUserId;
                    Timestamp ts = rs.getTimestamp("CommentTime");
                    comments.add(new ChatComment(
                            rs.getInt("CommentID"),
                            rs.getInt("WishlistID"),
                            userId,
                            rs.getString("Username"),
                            rs.getString("Icon"),
                            rs.getString("CommentText"),
                            CommentType.fromCode(rs.getInt("CommentType")),
                            ts != null ? ts.toLocalDateTime() : null
                    ));
                }
            }
            return comments;
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to fetch wishlist comments", ex);
        }
    }

    @Override
    public void addUserComment(int wishlistId, int userId, String commentText) {
        String sql = "INSERT INTO WishlistComments (WishlistID, UserID, CommentText, CommentType, CommentTime) "
                + "VALUES (?, ?, ?, 1, CURRENT_TIMESTAMP)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, wishlistId);
            stmt.setInt(2, userId);
            stmt.setString(3, commentText);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to add user comment", ex);
        }
    }

    @Override
    public void addEventComment(int wishlistId, String commentText) {
        String sql = "INSERT INTO WishlistComments (WishlistID, CommentText, CommentType, CommentTime) "
                + "VALUES (?, ?, 2, CURRENT_TIMESTAMP)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, wishlistId);
            stmt.setString(2, commentText);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to add event comment", ex);
        }
    }

    @Override
    public boolean withdrawLastUserComment(int wishlistId, int userId, String withdrawnNotice) {
        String sql = "UPDATE WishlistComments "
                + "SET CommentText = ?, CommentType = 2 "
                + "WHERE CommentID = ("
                + "SELECT CommentID FROM WishlistComments "
                + "WHERE WishlistID = ? AND UserID = ? AND CommentType = 1 "
                + "ORDER BY CommentTime DESC, CommentID DESC LIMIT 1)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, withdrawnNotice);
            stmt.setInt(2, wishlistId);
            stmt.setInt(3, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to withdraw last user comment", ex);
        }
    }
}
