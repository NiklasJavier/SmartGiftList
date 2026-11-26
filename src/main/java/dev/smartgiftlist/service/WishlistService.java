package dev.smartgiftlist.service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import dev.smartgiftlist.model.ChatComment;
import dev.smartgiftlist.model.DashboardData;
import dev.smartgiftlist.model.FlashMessage;
import dev.smartgiftlist.model.Gift;
import dev.smartgiftlist.model.GiftClaimer;
import dev.smartgiftlist.model.IconCatalog;
import dev.smartgiftlist.model.Wishlist;
import dev.smartgiftlist.model.WishlistParticipant;
import dev.smartgiftlist.repository.CommentRepository;
import dev.smartgiftlist.repository.GiftRepository;
import dev.smartgiftlist.repository.WishlistRepository;
import dev.smartgiftlist.security.HtmlUtil;

/**
 * Wishlist lifecycle, invite-code sharing, and dashboard read-model assembly.
 */
public class WishlistService {

    private static final String NET_ID_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int MAX_TITLE_LENGTH = 150;
    private static final int MAX_NET_ID_ATTEMPTS = 12;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final WishlistRepository wishlistRepository;
    private final GiftRepository giftRepository;
    private final CommentRepository commentRepository;

    public WishlistService(
            WishlistRepository wishlistRepository,
            GiftRepository giftRepository,
            CommentRepository commentRepository
    ) {
        this.wishlistRepository = wishlistRepository;
        this.giftRepository = giftRepository;
        this.commentRepository = commentRepository;
    }

    public ServiceResult<Wishlist> createWishlist(
            int ownerUserId,
            String rawTitle,
            String rawEvent,
            String rawEventDate,
            String rawDescription,
            String rawUrl,
            String rawIcon
    ) {
        String title = rawTitle != null ? rawTitle.trim() : "";
        if (title.isEmpty() || title.length() > MAX_TITLE_LENGTH) {
            return ServiceResult.failure(FlashMessage.error(
                    "Invalid wishlist title.",
                    "Please enter a wishlist title between 1 and " + MAX_TITLE_LENGTH + " characters."
            ));
        }

        LocalDate eventDate = parseDateLenient(rawEventDate);
        String event = rawEvent != null ? rawEvent.trim() : "";
        String description = rawDescription != null ? rawDescription.trim() : "";
        String safeUrl = HtmlUtil.sanitizeHttpUrl(rawUrl);
        String safeCover = IconCatalog.normalizeCoverPath(rawIcon);
        String netId = generateUniqueNetId();

        Wishlist created = wishlistRepository.create(
                ownerUserId, netId, title, event, eventDate, description, safeUrl, safeCover
        );
        return ServiceResult.success(created, FlashMessage.banner("Wishlist created."));
    }

    public DashboardData loadDashboardData(int userId, String preferredNetId, int selectedGiftId) {
        List<Wishlist> owned = wishlistRepository.findOwnedByUser(userId);
        List<Wishlist> connected = wishlistRepository.findConnectedByUser(userId);

        Wishlist active = resolveActiveWishlist(userId, preferredNetId)
                .orElseGet(() -> wishlistRepository.findLatestAccessibleByUser(userId).orElse(null));

        if (active == null) {
            return new DashboardData(owned, connected, null, false, List.of(), List.of(), List.of(), null, List.of());
        }

        boolean isOwner = active.getOwnerUserId() == userId;
        List<WishlistParticipant> participants = wishlistRepository.findParticipants(active.getId());
        List<Gift> gifts = giftRepository.findByWishlistIdWithClaims(active.getId());
        List<ChatComment> comments = commentRepository.findByWishlistId(active.getId());

        Gift selectedGift = null;
        List<GiftClaimer> claimers = List.of();
        if (selectedGiftId > 0) {
            Optional<Gift> matched = gifts.stream().filter(g -> g.getId() == selectedGiftId).findFirst();
            if (matched.isPresent()) {
                selectedGift = matched.get();
                claimers = giftRepository.findClaimers(selectedGift.getId());
            }
        }

        return new DashboardData(
                owned,
                connected,
                active,
                isOwner,
                participants,
                gifts,
                comments,
                selectedGift,
                claimers
        );
    }

    public Optional<Wishlist> findAccessibleByNetId(int userId, String rawNetId) {
        if (rawNetId == null || rawNetId.isBlank()) {
            return Optional.empty();
        }
        return wishlistRepository.findByNetId(rawNetId.trim())
                .filter(w -> wishlistRepository.isAccessibleByUser(w.getId(), userId));
    }

    public ServiceResult<Wishlist> updateWishlistDetails(
            int userId,
            int wishlistId,
            String rawUrl,
            String rawEvent,
            String rawDescription,
            String rawIcon,
            String rawEventDate
    ) {
        if (wishlistId <= 0 || !wishlistRepository.isOwnedByUser(wishlistId, userId)) {
            return ServiceResult.failure(FlashMessage.error(
                    "Permission denied.",
                    "Only the owner can update this wishlist."
            ));
        }

        String safeUrl = HtmlUtil.sanitizeHttpUrl(rawUrl);
        String event = rawEvent != null ? rawEvent.trim() : "";
        String description = rawDescription != null ? rawDescription.trim() : "";
        String safeCover = IconCatalog.normalizeCoverPath(rawIcon);
        LocalDate eventDate = parseDateLenient(rawEventDate);

        wishlistRepository.updateDetails(wishlistId, safeUrl, event, description, safeCover, eventDate);
        Wishlist updated = wishlistRepository.findById(wishlistId).orElse(null);
        return ServiceResult.success(updated, FlashMessage.banner("Wishlist updated."));
    }

    public ServiceResult<Boolean> deleteOrLeaveWishlist(int userId, int wishlistId) {
        if (wishlistId <= 0 || !wishlistRepository.isAccessibleByUser(wishlistId, userId)) {
            return ServiceResult.failure(FlashMessage.error(
                    "Wishlist unavailable.",
                    "The selected wishlist could not be modified."
            ));
        }
        if (wishlistRepository.isOwnedByUser(wishlistId, userId)) {
            wishlistRepository.deleteById(wishlistId);
            return ServiceResult.success(true, FlashMessage.banner("Wishlist removed."));
        }
        wishlistRepository.removeParticipant(wishlistId, userId);
        return ServiceResult.success(true, FlashMessage.banner("You left the shared wishlist."));
    }

    public ServiceResult<Wishlist> connectByNetId(int userId, String rawNetId) {
        String netId = rawNetId != null ? rawNetId.trim() : "";
        if (netId.isEmpty()) {
            return ServiceResult.failure(FlashMessage.error(
                    "Connection failed.",
                    "Unable to connect this wishlist to your account."
            ));
        }

        Optional<Wishlist> targetOpt = wishlistRepository.findByNetId(netId);
        if (targetOpt.isEmpty()) {
            return ServiceResult.failure(FlashMessage.error(
                    "Connection failed.",
                    "Unable to connect this wishlist to your account."
            ));
        }

        Wishlist target = targetOpt.get();
        if (target.getOwnerUserId() == userId) {
            return ServiceResult.failure(FlashMessage.error(
                    "Already your wishlist.",
                    "Unable to connect your own wishlist as a shared list."
            ));
        }

        boolean added = wishlistRepository.addParticipant(target.getId(), userId);
        if (!added) {
            return ServiceResult.failure(FlashMessage.error(
                    "Already connected.",
                    "Unable to connect this wishlist to your account (already connected)."
            ));
        }
        return ServiceResult.success(target, FlashMessage.banner("Wishlist connected to your account."));
    }

    private Optional<Wishlist> resolveActiveWishlist(int userId, String preferredNetId) {
        if (preferredNetId == null || preferredNetId.isBlank()) {
            return Optional.empty();
        }
        return findAccessibleByNetId(userId, preferredNetId);
    }

    private String generateUniqueNetId() {
        for (int attempt = 0; attempt < MAX_NET_ID_ATTEMPTS; attempt++) {
            String candidate = "SGL-" + randomSegment(4) + "-" + randomSegment(4);
            if (!wishlistRepository.existsByNetId(candidate)) {
                return candidate;
            }
        }
        return "SGL-" + Long.toString(System.nanoTime(), 36).toUpperCase();
    }

    private static String randomSegment(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(NET_ID_ALPHABET.charAt(RANDOM.nextInt(NET_ID_ALPHABET.length())));
        }
        return sb.toString();
    }

    private static LocalDate parseDateLenient(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw.trim());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }
}
