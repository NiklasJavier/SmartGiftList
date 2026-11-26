package dev.smartgiftlist.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import dev.smartgiftlist.model.Gift;
import dev.smartgiftlist.model.GiftClaimer;

/**
 * Persistence port for gift items and participant claims.
 */
public interface GiftRepository {

    enum ClaimOutcome {
        CLAIMED,
        ALREADY_CLAIMED,
        CAPACITY_FULL,
        NOT_FOUND
    }

    Gift create(
            int wishlistId,
            String title,
            String description,
            String url,
            String icon,
            BigDecimal price,
            int numberOfPeople,
            boolean surprise,
            int creatorUserId
    );

    Optional<Gift> findById(int giftId);

    List<Gift> findByWishlistIdWithClaims(int wishlistId);

    boolean deleteById(int giftId);

    List<GiftClaimer> findClaimers(int giftId);

    ClaimOutcome claimGiftAtomically(int giftId, int userId);

    boolean releaseClaim(int giftId, int userId);
}
