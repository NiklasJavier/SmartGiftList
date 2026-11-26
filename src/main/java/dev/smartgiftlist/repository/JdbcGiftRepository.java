package dev.smartgiftlist.repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

import dev.smartgiftlist.model.Gift;
import dev.smartgiftlist.model.GiftClaimer;
import dev.smartgiftlist.model.IconCatalog;

/**
 * Pooled JDBC implementation of {@link GiftRepository} using row locking for atomic claims.
 */
public class JdbcGiftRepository implements GiftRepository {

    private final DataSource dataSource;

    public JdbcGiftRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Gift create(
            int wishlistId,
            String title,
            String description,
            String url,
            String icon,
            BigDecimal price,
            int numberOfPeople,
            boolean surprise,
            int creatorUserId
    ) {
        String safeCover = IconCatalog.normalizeCoverPath(icon);
        BigDecimal safePrice = price != null ? price : BigDecimal.ZERO;
        int safePeople = Math.max(numberOfPeople, 1);
        String sql = "INSERT INTO Gifts "
                + "(WishlistID, Title, Description, Url, Icon, Price, NumberOfPeople, Surprise, CreatorUserID, CreateDate) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP) RETURNING GiftID";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, wishlistId);
            stmt.setString(2, title);
            stmt.setString(3, description);
            stmt.setString(4, url);
            stmt.setString(5, safeCover);
            stmt.setBigDecimal(6, safePrice);
            stmt.setInt(7, safePeople);
            stmt.setBoolean(8, surprise);
            stmt.setInt(9, creatorUserId);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                int giftId = rs.getInt("GiftID");
                return new Gift(
                        giftId,
                        wishlistId,
                        title,
                        description,
                        url,
                        safeCover,
                        safePrice,
                        safePeople,
                        surprise,
                        creatorUserId,
                        0,
                        List.of()
                );
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to create gift", ex);
        }
    }

    @Override
    public Optional<Gift> findById(int giftId) {
        String sql = "SELECT g.GiftID, g.WishlistID, g.Title, g.Description, g.Url, g.Icon, g.Price, "
                + "g.NumberOfPeople, g.Surprise, g.CreatorUserID, "
                + "COUNT(ugc.UserGiftConnectionID) AS ClaimCount, "
                + "COALESCE(STRING_AGG(u.Icon, ',' ORDER BY ugc.LinkDate, ugc.UserID), '') AS ClaimerIcons "
                + "FROM Gifts g "
                + "LEFT JOIN UserGiftConnections ugc ON g.GiftID = ugc.GiftID "
                + "LEFT JOIN Users u ON ugc.UserID = u.UserID "
                + "WHERE g.GiftID = ? "
                + "GROUP BY g.GiftID, g.WishlistID, g.Title, g.Description, g.Url, g.Icon, g.Price, "
                + "g.NumberOfPeople, g.Surprise, g.CreatorUserID";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, giftId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapGift(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to load gift by ID", ex);
        }
    }

    @Override
    public List<Gift> findByWishlistIdWithClaims(int wishlistId) {
        String sql = "SELECT g.GiftID, g.WishlistID, g.Title, g.Description, g.Url, g.Icon, g.Price, "
                + "g.NumberOfPeople, g.Surprise, g.CreatorUserID, "
                + "COUNT(ugc.UserGiftConnectionID) AS ClaimCount, "
                + "COALESCE(STRING_AGG(u.Icon, ',' ORDER BY ugc.LinkDate, ugc.UserID), '') AS ClaimerIcons "
                + "FROM Gifts g "
                + "LEFT JOIN UserGiftConnections ugc ON g.GiftID = ugc.GiftID "
                + "LEFT JOIN Users u ON ugc.UserID = u.UserID "
                + "WHERE g.WishlistID = ? "
                + "GROUP BY g.GiftID, g.WishlistID, g.Title, g.Description, g.Url, g.Icon, g.Price, "
                + "g.NumberOfPeople, g.Surprise, g.CreatorUserID "
                + "ORDER BY g.GiftID";
        List<Gift> gifts = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, wishlistId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    gifts.add(mapGift(rs));
                }
            }
            return gifts;
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to list gifts for wishlist", ex);
        }
    }

    @Override
    public boolean deleteById(int giftId) {
        String sql = "DELETE FROM Gifts WHERE GiftID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, giftId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to delete gift", ex);
        }
    }

    @Override
    public List<GiftClaimer> findClaimers(int giftId) {
        String sql = "SELECT u.UserID, u.Username, u.Icon "
                + "FROM UserGiftConnections ugc "
                + "JOIN Users u ON ugc.UserID = u.UserID "
                + "WHERE ugc.GiftID = ? ORDER BY ugc.LinkDate, u.UserID";
        List<GiftClaimer> claimers = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, giftId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    claimers.add(new GiftClaimer(
                            rs.getInt("UserID"),
                            rs.getString("Username"),
                            rs.getString("Icon")
                    ));
                }
            }
            return claimers;
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to load gift claimers", ex);
        }
    }

    @Override
    public ClaimOutcome claimGiftAtomically(int giftId, int userId) {
        String lockGiftSql = "SELECT NumberOfPeople FROM Gifts WHERE GiftID = ? FOR UPDATE";
        String dupSql = "SELECT 1 FROM UserGiftConnections WHERE GiftID = ? AND UserID = ?";
        String countSql = "SELECT COUNT(*) FROM UserGiftConnections WHERE GiftID = ?";
        String insertSql = "INSERT INTO UserGiftConnections (UserID, GiftID, LinkDate) VALUES (?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = dataSource.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                int maxPeople;
                try (PreparedStatement lockStmt = conn.prepareStatement(lockGiftSql)) {
                    lockStmt.setInt(1, giftId);
                    try (ResultSet rs = lockStmt.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return ClaimOutcome.NOT_FOUND;
                        }
                        maxPeople = rs.getInt("NumberOfPeople");
                    }
                }

                try (PreparedStatement dupStmt = conn.prepareStatement(dupSql)) {
                    dupStmt.setInt(1, giftId);
                    dupStmt.setInt(2, userId);
                    try (ResultSet rs = dupStmt.executeQuery()) {
                        if (rs.next()) {
                            conn.rollback();
                            return ClaimOutcome.ALREADY_CLAIMED;
                        }
                    }
                }

                int currentClaims = 0;
                try (PreparedStatement countStmt = conn.prepareStatement(countSql)) {
                    countStmt.setInt(1, giftId);
                    try (ResultSet rs = countStmt.executeQuery()) {
                        if (rs.next()) {
                            currentClaims = rs.getInt(1);
                        }
                    }
                }
                if (maxPeople > 0 && currentClaims >= maxPeople) {
                    conn.rollback();
                    return ClaimOutcome.CAPACITY_FULL;
                }

                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, userId);
                    insertStmt.setInt(2, giftId);
                    insertStmt.executeUpdate();
                }

                conn.commit();
                return ClaimOutcome.CLAIMED;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to claim gift atomically", ex);
        }
    }

    @Override
    public boolean releaseClaim(int giftId, int userId) {
        String sql = "DELETE FROM UserGiftConnections WHERE GiftID = ? AND UserID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, giftId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to release gift claim", ex);
        }
    }

    private static Gift mapGift(ResultSet rs) throws SQLException {
        String rawIcons = rs.getString("ClaimerIcons");
        List<String> icons = (rawIcons == null || rawIcons.isBlank())
                ? List.of()
                : Arrays.stream(rawIcons.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
        return new Gift(
                rs.getInt("GiftID"),
                rs.getInt("WishlistID"),
                rs.getString("Title"),
                rs.getString("Description"),
                rs.getString("Url"),
                rs.getString("Icon"),
                rs.getBigDecimal("Price"),
                rs.getInt("NumberOfPeople"),
                rs.getBoolean("Surprise"),
                rs.getInt("CreatorUserID"),
                rs.getInt("ClaimCount"),
                icons
        );
    }
}
