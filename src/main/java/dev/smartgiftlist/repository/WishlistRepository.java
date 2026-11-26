package dev.smartgiftlist.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import dev.smartgiftlist.model.Wishlist;
import dev.smartgiftlist.model.WishlistParticipant;

/**
 * Persistence port for wishlists and participant memberships.
 */
public interface WishlistRepository {

    Wishlist create(
            int ownerUserId,
            String netId,
            String title,
            String event,
            LocalDate eventDate,
            String description,
            String url,
            String icon
    );

    Optional<Wishlist> findById(int wishlistId);

    Optional<Wishlist> findByNetId(String netId);

    List<Wishlist> findOwnedByUser(int userId);

    List<Wishlist> findConnectedByUser(int userId);

    Optional<Wishlist> findLatestAccessibleByUser(int userId);

    boolean updateDetails(
            int wishlistId,
            String url,
            String event,
            String description,
            String icon,
            LocalDate eventDate
    );

    boolean deleteById(int wishlistId);

    boolean isOwnedByUser(int wishlistId, int userId);

    boolean isAccessibleByUser(int wishlistId, int userId);

    boolean addParticipant(int wishlistId, int userId);

    boolean removeParticipant(int wishlistId, int userId);

    List<WishlistParticipant> findParticipants(int wishlistId);

    boolean existsByNetId(String netId);
}
