package dev.smartgiftlist.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import dev.smartgiftlist.security.HtmlUtil;

/**
 * Immutable gift item domain model with status and formatting helpers for views.
 */
public final class Gift {

    private static final int TABLE_TITLE_LIMIT = 12;
    private static final int TABLE_DESC_LIMIT = 33;

    private final int id;
    private final int wishlistId;
    private final String title;
    private final String description;
    private final String url;
    private final String icon;
    private final BigDecimal price;
    private final int numberOfPeople;
    private final boolean surprise;
    private final int creatorUserId;
    private final int claimCount;
    private final List<String> claimerAvatarPaths;

    public Gift(
            int id,
            int wishlistId,
            String title,
            String description,
            String url,
            String icon,
            BigDecimal price,
            int numberOfPeople,
            boolean surprise,
            int creatorUserId,
            int claimCount,
            List<String> claimerIcons
    ) {
        this.id = id;
        this.wishlistId = wishlistId;
        this.title = title != null ? title : "";
        this.description = description != null ? description : "";
        this.url = url != null ? url : "";
        this.icon = IconCatalog.normalizeCoverPath(icon);
        this.price = price != null ? price : BigDecimal.ZERO;
        this.numberOfPeople = Math.max(numberOfPeople, 1);
        this.surprise = surprise;
        this.creatorUserId = creatorUserId;
        this.claimCount = Math.max(claimCount, 0);
        this.claimerAvatarPaths = claimerIcons == null
                ? List.of()
                : claimerIcons.stream().map(IconCatalog::avatarPathForKey).toList();
    }

    public int getId() {
        return id;
    }

    public int getWishlistId() {
        return wishlistId;
    }

    public String getTitle() {
        return title;
    }

    public String getShortTitle() {
        return HtmlUtil.truncateChars(title, TABLE_TITLE_LIMIT);
    }

    public String getDescription() {
        return description;
    }

    public String getShortDescription() {
        return HtmlUtil.truncateChars(description, TABLE_DESC_LIMIT);
    }

    public String getUrl() {
        return url;
    }

    public String getIcon() {
        return icon;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getFormattedPrice() {
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            return "";
        }
        BigDecimal scaled = price.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
        return scaled.scale() <= 0 ? price.setScale(0, RoundingMode.HALF_UP).toPlainString() : scaled.toPlainString();
    }

    public int getNumberOfPeople() {
        return numberOfPeople;
    }

    public boolean isSurprise() {
        return surprise;
    }

    public int getCreatorUserId() {
        return creatorUserId;
    }

    public int getClaimCount() {
        return claimCount;
    }

    public boolean isFullyClaimed() {
        return numberOfPeople > 0 && claimCount >= numberOfPeople;
    }

    public List<String> getClaimerAvatarPaths() {
        return claimerAvatarPaths;
    }

    public GiftStatus getStatus() {
        if (isFullyClaimed()) {
            return GiftStatus.CLAIMED;
        }
        if (surprise) {
            return GiftStatus.SURPRISE;
        }
        return numberOfPeople == 1 ? GiftStatus.SINGLE : GiftStatus.GROUP;
    }

    public String getStatusKey() {
        return getStatus().getKey();
    }

    public String getTypeBadges() {
        StringBuilder badges = new StringBuilder();
        if (surprise) {
            badges.append("SG");
        } else if (numberOfPeople == 1) {
            badges.append("IG");
        } else {
            badges.append("GG");
        }
        if (isFullyClaimed()) {
            badges.append(", CL");
        }
        return badges.toString();
    }

    public String getDetailTypeLabel() {
        if (numberOfPeople == 1) {
            return surprise ? "Surprise Gift, Individual Gift" : "Individual Gift";
        }
        return surprise ? "Surprise Gift, Group Gift" : "Group Gift";
    }
}
