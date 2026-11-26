package dev.smartgiftlist.beans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dev.smartgiftlist.security.HtmlUtil;

/**
 * Manages wishlist discussion comments and event messages.
 */
public class ChatManager {

    private final Connection jdbcConn;

    public ChatManager(Connection jdbcConn) {
        this.jdbcConn = jdbcConn;
    }

    public String getRenderWishlistComments(int wishlistID) throws SQLException {
        if (wishlistID != 0) {
            List<ChatMessage> messages = fetchCommentsForWishlist(wishlistID);
            return renderMessages(messages);
        }
        return "";
    }

    private List<ChatMessage> fetchCommentsForWishlist(int wishlistID) throws SQLException {
        List<ChatMessage> messages = new ArrayList<>();
        String sql = "SELECT wc.CommentText, wc.CommentType, u.UserID, u.Username, u.Icon "
                + "FROM WishlistComments wc "
                + "LEFT JOIN Users u ON wc.UserID = u.UserID "
                + "WHERE wc.WishlistID = ? ORDER BY wc.CommentTime DESC";
        try (PreparedStatement stmt = jdbcConn.prepareStatement(sql)) {
            stmt.setInt(1, wishlistID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String commentText = rs.getString("CommentText");
                    int commentType = rs.getInt("CommentType");
                    String iconName = rs.getString("Icon");
                    String userIcon = "../img/anlaesse/" + (iconName != null ? iconName : "Lumibella") + ".webp";
                    String userName = rs.getString("Username");
                    messages.add(new ChatMessage(commentType == 1 ? "normal" : "event", userIcon, userName, commentText));
                }
            }
        }
        return messages;
    }

    private String renderMessages(List<ChatMessage> messages) {
        StringBuilder html = new StringBuilder();
        for (ChatMessage message : messages) {
            if ("normal".equals(message.getType())) {
                html.append(renderUserMessage(message.getUserIcon(), message.getUserName(), message.getText()));
            } else if ("event".equals(message.getType())) {
                html.append(renderEventMessage(message.getText()));
            }
        }
        return html.toString();
    }

    private String renderUserMessage(String iconPath, String userName, String text) {
        String safeUser = HtmlUtil.escape(userName);
        String safeText = HtmlUtil.escape(text);
        String prefix = safeUser.isEmpty() ? "" : "<strong>" + safeUser + ":</strong> ";
        return String.format(
                "<div class=\"message user-message\">"
                + "<div class=\"user-initials\"><img src=\"%s\" alt=\"Profile picture\" class=\"owlp-profile-img\"></div>"
                + "<div class=\"user-text\">%s%s</div>"
                + "</div>",
                HtmlUtil.escape(iconPath), prefix, safeText);
    }

    private String renderEventMessage(String text) {
        return String.format("<div class=\"message\">%s</div>", HtmlUtil.escape(text));
    }

    public void addEventMessage(int wishlistID, String commentText) throws SQLException {
        if (wishlistID != 0 && commentText != null && !commentText.isBlank()) {
            String sql = "INSERT INTO WishlistComments (WishlistID, CommentText, CommentType, CommentTime) VALUES (?, ?, 2, CURRENT_TIMESTAMP)";
            try (PreparedStatement stmt = jdbcConn.prepareStatement(sql)) {
                stmt.setInt(1, wishlistID);
                stmt.setString(2, commentText);
                stmt.executeUpdate();
            }
        }
    }

    public void addNormalMessage(int wishlistID, int userID, String commentText) throws SQLException {
        if (wishlistID != 0 && userID != 0 && commentText != null && !commentText.isBlank()) {
            String sql = "INSERT INTO WishlistComments (WishlistID, UserID, CommentText, CommentType, CommentTime) VALUES (?, ?, ?, 1, CURRENT_TIMESTAMP)";
            try (PreparedStatement stmt = jdbcConn.prepareStatement(sql)) {
                stmt.setInt(1, wishlistID);
                stmt.setInt(2, userID);
                stmt.setString(3, commentText);
                stmt.executeUpdate();
            }
        }
    }

    public void deleteEventMessage(int commentID) throws SQLException {
        String sql = "DELETE FROM WishlistComments WHERE CommentID = ? AND CommentType = 2";
        try (PreparedStatement stmt = jdbcConn.prepareStatement(sql)) {
            stmt.setInt(1, commentID);
            stmt.executeUpdate();
        }
    }

    public void withdrawLastUserMessage(int userID, int wishlistID) throws SQLException {
        if (userID != 0 && wishlistID != 0) {
            String findLastMessageSql = "SELECT CommentID FROM WishlistComments WHERE UserID = ? AND WishlistID = ? ORDER BY CommentTime DESC LIMIT 1";
            int lastMessageID = -1;
            try (PreparedStatement stmt = jdbcConn.prepareStatement(findLastMessageSql)) {
                stmt.setInt(1, userID);
                stmt.setInt(2, wishlistID);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        lastMessageID = rs.getInt("CommentID");
                    }
                }
            }

            if (lastMessageID != -1) {
                String updateMessageSql = "UPDATE WishlistComments SET CommentText = 'This message was withdrawn by the participant.', CommentType = 2 WHERE CommentID = ?";
                try (PreparedStatement stmt = jdbcConn.prepareStatement(updateMessageSql)) {
                    stmt.setInt(1, lastMessageID);
                    stmt.executeUpdate();
                }
            }
        }
    }
}
