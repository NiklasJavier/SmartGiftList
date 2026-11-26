package dev.smartgiftlist.service;

import java.math.BigDecimal;
import java.util.Optional;

import dev.smartgiftlist.model.FlashMessage;
import dev.smartgiftlist.model.Gift;
import dev.smartgiftlist.model.IconCatalog;
import dev.smartgiftlist.repository.GiftRepository;
import dev.smartgiftlist.repository.GiftRepository.ClaimOutcome;
import dev.smartgiftlist.repository.WishlistRepository;
import dev.smartgiftlist.security.HtmlUtil;

/**
 * Gift management and authorization service.
 */
public class GiftService {

    private static final int MAX_PARTICIPANTS = 100;

    private final GiftRepository giftRepository;
    private final WishlistRepository wishlistRepository;

    public GiftService(GiftRepository giftRepository, WishlistRepository wishlistRepository) {
        this.giftRepository = giftRepository;
        this.wishlistRepository = wishlistRepository;
    }

    public ServiceResult<Gift> addGift(
            int userId,
            int wishlistId,
            String rawTitle,
            String rawDescription,
            String rawUrl,
            String rawIcon,
            String rawPrice,
            String rawPeople,
            boolean surprise
    ) {
        if (wishlistId <= 0 || !wishlistRepository.isAccessibleByUser(wishlistId, userId)) {
            return ServiceResult.failure(FlashMessage.error(
                    "Wishlist unavailable.",
                    "Please select or create a wishlist before adding a gift."
            ));
        }

        String title = rawTitle != null ? rawTitle.trim() : "";
        if (title.isEmpty()) {
            return ServiceResult.failure(FlashMessage.error(
                    "Gift title missing.",
                    "Please enter a gift name."
            ));
        }

        String description = rawDescription != null ? rawDescription.trim() : "";
        String safeUrl = HtmlUtil.sanitizeHttpUrl(rawUrl);
        String safeCover = IconCatalog.normalizeCoverPath(rawIcon);
        BigDecimal price = parsePriceOrZero(rawPrice);
        int numberOfPeople = clampParticipants(parseIntOrDefault(rawPeople, 1));

        Gift created = giftRepository.create(
                wishlistId, title, description, safeUrl, safeCover, price, numberOfPeople, surprise, userId
        );
        return ServiceResult.success(created, FlashMessage.banner("Gift added to the wishlist."));
    }

    public Optional<Gift> findAccessibleGift(int userId, int giftId) {
        if (userId <= 0 || giftId <= 0) {
            return Optional.empty();
        }
        return giftRepository.findById(giftId)
                .filter(gift -> wishlistRepository.isAccessibleByUser(gift.getWishlistId(), userId));
    }

    public ServiceResult<Gift> claimGift(int userId, int giftId) {
        Optional<Gift> giftOpt = findAccessibleGift(userId, giftId);
        if (giftOpt.isEmpty()) {
            return ServiceResult.failure(FlashMessage.error(
                    "Gift unavailable.",
                    "Unable to access or claim this gift."
            ));
        }

        ClaimOutcome outcome = giftRepository.claimGiftAtomically(giftId, userId);
        return switch (outcome) {
            case CLAIMED -> ServiceResult.success(
                    giftRepository.findById(giftId).orElse(giftOpt.get()),
                    FlashMessage.banner("You have claimed this gift.")
            );
            case ALREADY_CLAIMED, CAPACITY_FULL -> ServiceResult.failure(FlashMessage.error(
                    "Claim declined.",
                    "Unable to claim this gift (already full or already claimed by you)."
            ));
            case NOT_FOUND -> ServiceResult.failure(FlashMessage.error(
                    "Gift unavailable.",
                    "Gift not found."
            ));
        };
    }

    public ServiceResult<Gift> releaseGift(int userId, int giftId) {
        Optional<Gift> giftOpt = findAccessibleGift(userId, giftId);
        if (giftOpt.isEmpty()) {
            return ServiceResult.failure(FlashMessage.error(
                    "Gift unavailable.",
                    "Unable to access this gift."
            ));
        }
        giftRepository.releaseClaim(giftId, userId);
        return ServiceResult.success(
                giftRepository.findById(giftId).orElse(giftOpt.get()),
                FlashMessage.banner("You have released your claim on this gift.")
        );
    }

    public ServiceResult<Boolean> deleteGift(int userId, int giftId) {
        Optional<Gift> giftOpt = giftRepository.findById(giftId);
        if (giftOpt.isEmpty()) {
            return ServiceResult.failure(FlashMessage.error(
                    "No gift selected.",
                    "No gift was selected."
            ));
        }
        Gift gift = giftOpt.get();
        boolean isCreator = gift.getCreatorUserId() == userId;
        boolean isListOwner = wishlistRepository.isOwnedByUser(gift.getWishlistId(), userId);
        if (!isCreator && !isListOwner) {
            return ServiceResult.failure(FlashMessage.error(
                    "Permission denied.",
                    "Only the wishlist owner or gift creator can remove this gift."
            ));
        }
        giftRepository.deleteById(giftId);
        return ServiceResult.success(true, FlashMessage.banner("Gift removed from the wishlist."));
    }

    private static BigDecimal parsePriceOrZero(String rawPrice) {
        if (rawPrice == null || rawPrice.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            String normalized = rawPrice.trim().replace(',', '.');
            BigDecimal parsed = new BigDecimal(normalized);
            return parsed.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : parsed;
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private static int parseIntOrDefault(String raw, int fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static int clampParticipants(int value) {
        if (value < 1) {
            return 1;
        }
        return Math.min(value, MAX_PARTICIPANTS);
    }
}
