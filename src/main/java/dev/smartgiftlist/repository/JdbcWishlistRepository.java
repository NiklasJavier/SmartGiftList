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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

import dev.smartgiftlist.model.IconCatalog;
import dev.smartgiftlist.model.Wishlist;
import dev.smartgiftlist.model.WishlistParticipant;

/**
 * Pooled JDBC implementation of {@link WishlistRepository}.
 */
public class JdbcWishlistRepository implements WishlistRepository {

    private static final String SELECT_COLS =
            "WishlistID, NetID, Title, Event, EventDate, Description, URL, Icon, UserID, CreateDate ";

    private final DataSource dataSource;

    public JdbcWishlistRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Wishlist create(
            int ownerUserId,
            String netId,
            String title,
            String event,
            LocalDate eventDate,
            String description,
            String url,
            String icon
    ) {
        String safeCover = IconCatalog.normalizeCoverPath(icon);
        String sql = "INSERT INTO Wishlists (UserID, NetID, Title, Event, Description, URL, Icon, EventDate, CreateDate) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP) RETURNING WishlistID, CreateDate";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ownerUserId);
            stmt.setString(2, netId);
            stmt.setString(3, title);
            stmt.setString(4, event);
            stmt.setString(5, description);
            stmt.setString(6, url);
            stmt.setString(7, safeCover);
            if (eventDate != null) {
                stmt.setDate(8, Date.valueOf(eventDate));
            } else {
                stmt.setNull(8, Types.DATE);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                int id = rs.getInt("WishlistID");
                Timestamp createdTs = rs.getTimestamp("CreateDate");
                LocalDateTime createdAt = createdTs != null ? createdTs.toLocalDateTime() : LocalDateTime.now();
                return new Wishlist(id, netId, title, event, eventDate, description, url, safeCover, ownerUserId, createdAt);
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to create wishlist", ex);
        }
    }

    @Override
    public Optional<Wishlist> findById(int wishlistId) {
        String sql = "SELECT " + SELECT_COLS + "FROM Wishlists WHERE WishlistID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, wishlistId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapWishlist(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to find wishlist by ID", ex);
        }
    }

    @Override
    public Optional<Wishlist> findByNetId(String netId) {
        String sql = "SELECT " + SELECT_COLS + "FROM Wishlists WHERE NetID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, netId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapWishlist(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to find wishlist by NetID", ex);
        }
    }

    @Override
    public List<Wishlist> findOwnedByUser(int userId) {
        String sql = "SELECT " + SELECT_COLS + "FROM Wishlists WHERE UserID = ? ORDER BY WishlistID";
        return queryWishlists(sql, userId);
    }

    @Override
    public List<Wishlist> findConnectedByUser(int userId) {
        String sql = "SELECT w.WishlistID, w.NetID, w.Title, w.Event, w.EventDate, w.Description, "
                + "w.URL, w.Icon, w.UserID, w.CreateDate "
                + "FROM Wishlists w "
                + "JOIN UserWishlistConnections uwc ON w.WishlistID = uwc.WishlistID "
                + "WHERE uwc.UserID = ? ORDER BY w.WishlistID";
        return queryWishlists(sql, userId);
    }

    @Override
    public Optional<Wishlist> findLatestAccessibleByUser(int userId) {
        String sql = "SELECT w.WishlistID, w.NetID, w.Title, w.Event, w.EventDate, w.Description, "
                + "w.URL, w.Icon, w.UserID, w.CreateDate "
                + "FROM Wishlists w "
                + "WHERE w.UserID = ? "
                + "OR EXISTS (SELECT 1 FROM UserWishlistConnections uwc WHERE uwc.WishlistID = w.WishlistID AND uwc.UserID = ?) "
                + "ORDER BY w.CreateDate DESC, w.WishlistID DESC LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapWishlist(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to load latest wishlist for user", ex);
        }
    }

    @Override
    public boolean updateDetails(
            int wishlistId,
            String url,
            String event,
            String description,
            String icon,
            LocalDate eventDate
    ) {
        String safeCover = IconCatalog.normalizeCoverPath(icon);
        String sql = "UPDATE Wishlists SET URL = ?, Event = ?, Description = ?, Icon = ?, EventDate = ? WHERE WishlistID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, url);
            stmt.setString(2, event);
            stmt.setString(3, description);
            stmt.setString(4, safeCover);
            if (eventDate != null) {
                stmt.setDate(5, Date.valueOf(eventDate));
            } else {
                stmt.setNull(5, Types.DATE);
            }
            stmt.setInt(6, wishlistId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to update wishlist details", ex);
        }
    }

    @Override
    public boolean deleteById(int wishlistId) {
        String sql = "DELETE FROM Wishlists WHERE WishlistID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, wishlistId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to delete wishlist", ex);
        }
    }

    @Override
    public boolean isOwnedByUser(int wishlistId, int userId) {
        String sql = "SELECT 1 FROM Wishlists WHERE WishlistID = ? AND UserID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, wishlistId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to verify wishlist ownership", ex);
        }
    }

    @Override
    public boolean isAccessibleByUser(int wishlistId, int userId) {
        String sql = "SELECT 1 FROM Wishlists w WHERE w.WishlistID = ? "
                + "AND (w.UserID = ? OR EXISTS (SELECT 1 FROM UserWishlistConnections uwc "
                + "WHERE uwc.WishlistID = w.WishlistID AND uwc.UserID = ?))";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, wishlistId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to verify wishlist access", ex);
        }
    }

    @Override
    public boolean addParticipant(int wishlistId, int userId) {
        String sql = "INSERT INTO UserWishlistConnections (UserID, WishlistID, IsOwner, LinkDate) "
                + "VALUES (?, ?, FALSE, CURRENT_TIMESTAMP) ON CONFLICT (UserID, WishlistID) DO NOTHING";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, wishlistId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to connect participant to wishlist", ex);
        }
    }

    @Override
    public boolean removeParticipant(int wishlistId, int userId) {
        String sql = "DELETE FROM UserWishlistConnections WHERE WishlistID = ? AND UserID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, wishlistId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to remove participant from wishlist", ex);
        }
    }

    @Override
    public List<WishlistParticipant> findParticipants(int wishlistId) {
        String sql = "SELECT u.UserID, u.Username, u.Icon, TRUE AS IsOwner "
                + "FROM Wishlists w JOIN Users u ON w.UserID = u.UserID WHERE w.WishlistID = ? "
                + "UNION "
                + "SELECT u.UserID, u.Username, u.Icon, uwc.IsOwner "
                + "FROM UserWishlistConnections uwc JOIN Users u ON uwc.UserID = u.UserID "
                + "WHERE uwc.WishlistID = ? AND u.UserID <> (SELECT UserID FROM Wishlists WHERE WishlistID = ?) "
                + "ORDER BY IsOwner DESC, UserID ASC";
        List<WishlistParticipant> participants = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, wishlistId);
            stmt.setInt(2, wishlistId);
            stmt.setInt(3, wishlistId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    participants.add(new WishlistParticipant(
                            rs.getInt("UserID"),
                            rs.getString("Username"),
                            rs.getString("Icon"),
                            rs.getBoolean("IsOwner")
                    ));
                }
            }
            return participants;
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to load wishlist participants", ex);
        }
    }

    @Override
    public boolean existsByNetId(String netId) {
        String sql = "SELECT 1 FROM Wishlists WHERE NetID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, netId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to check NetID uniqueness", ex);
        }
    }

    private List<Wishlist> queryWishlists(String sql, int userId) {
        List<Wishlist> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapWishlist(rs));
                }
            }
            return results;
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to query wishlists", ex);
        }
    }

    private static Wishlist mapWishlist(ResultSet rs) throws SQLException {
        Date eventSql = rs.getDate("EventDate");
        Timestamp createTs = rs.getTimestamp("CreateDate");
        return new Wishlist(
                rs.getInt("WishlistID"),
                rs.getString("NetID"),
                rs.getString("Title"),
                rs.getString("Event"),
                eventSql != null ? eventSql.toLocalDate() : null,
                rs.getString("Description"),
                rs.getString("URL"),
                rs.getString("Icon"),
                rs.getInt("UserID"),
                createTs != null ? createTs.toLocalDateTime() : null
        );
    }
}
