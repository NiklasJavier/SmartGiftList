package dev.smartgiftlist.model;

import java.util.List;

/**
 * Aggregated read model rendered by the wishlists dashboard view.
 */
public final class DashboardData {

    private final List<Wishlist> ownedWishlists;
    private final List<Wishlist> connectedWishlists;
    private final Wishlist activeWishlist;
    private final boolean activeWishlistOwner;
    private final List<WishlistParticipant> participants;
    private final List<Gift> gifts;
    private final List<ChatComment> comments;
    private final Gift selectedGift;
    private final List<GiftClaimer> selectedGiftClaimers;

    public DashboardData(
            List<Wishlist> ownedWishlists,
            List<Wishlist> connectedWishlists,
            Wishlist activeWishlist,
            boolean activeWishlistOwner,
            List<WishlistParticipant> participants,
            List<Gift> gifts,
            List<ChatComment> comments,
            Gift selectedGift,
            List<GiftClaimer> selectedGiftClaimers
    ) {
        this.ownedWishlists = ownedWishlists != null ? List.copyOf(ownedWishlists) : List.of();
        this.connectedWishlists = connectedWishlists != null ? List.copyOf(connectedWishlists) : List.of();
        this.activeWishlist = activeWishlist;
        this.activeWishlistOwner = activeWishlistOwner;
        this.participants = participants != null ? List.copyOf(participants) : List.of();
        this.gifts = gifts != null ? List.copyOf(gifts) : List.of();
        this.comments = comments != null ? List.copyOf(comments) : List.of();
        this.selectedGift = selectedGift;
        this.selectedGiftClaimers = selectedGiftClaimers != null ? List.copyOf(selectedGiftClaimers) : List.of();
    }

    public List<Wishlist> getOwnedWishlists() {
        return ownedWishlists;
    }

    public List<Wishlist> getConnectedWishlists() {
        return connectedWishlists;
    }

    public Wishlist getActiveWishlist() {
        return activeWishlist;
    }

    public boolean isHasActiveWishlist() {
        return activeWishlist != null;
    }

    public boolean isActiveWishlistOwner() {
        return activeWishlistOwner;
    }

    public List<WishlistParticipant> getParticipants() {
        return participants;
    }

    public List<Gift> getGifts() {
        return gifts;
    }

    public List<ChatComment> getComments() {
        return comments;
    }

    public Gift getSelectedGift() {
        return selectedGift;
    }

    public List<GiftClaimer> getSelectedGiftClaimers() {
        return selectedGiftClaimers;
    }
}
