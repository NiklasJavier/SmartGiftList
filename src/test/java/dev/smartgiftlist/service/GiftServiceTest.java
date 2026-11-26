package dev.smartgiftlist.service;

import dev.smartgiftlist.model.Gift;
import dev.smartgiftlist.model.User;
import dev.smartgiftlist.model.Wishlist;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GiftServiceTest {

    @Test
    void enforcesAccessControlsCapacityLimitsAndCreatorDeleteRights() {
        InMemoryRepositories repo = new InMemoryRepositories();
        AuthService auth = new AuthService(repo.users());
        WishlistService wishlists = new WishlistService(repo.wishlists(), repo.gifts(), repo.comments());
        GiftService gifts = new GiftService(repo.gifts(), repo.wishlists());

        User alice = auth.register("alice", "a@test.example", "", "secret123", "secret123").getValue();
        User bob = auth.register("bob", "b@test.example", "", "secret123", "secret123").getValue();
        User charlie = auth.register("charlie", "c@test.example", "", "secret123", "secret123").getValue();
        Wishlist list = wishlists.createWishlist(alice.getId(), "Party", "", "", "", "", "PaperList").getValue();

        Gift groupGift = gifts.addGift(alice.getId(), list.getId(), "Coffee Machine", "Group espresso",
                "https://example.test/m", "GoldenList", "149.90", "2", false).getValue();

        // Bob cannot claim or delete before connecting
        assertFalse(gifts.claimGift(bob.getId(), groupGift.getId()).isOk());
        assertFalse(gifts.deleteGift(bob.getId(), groupGift.getId()).isOk());

        wishlists.connectByNetId(bob.getId(), list.getNetId());
        wishlists.connectByNetId(charlie.getId(), list.getNetId());

        assertTrue(gifts.claimGift(alice.getId(), groupGift.getId()).isOk());
        assertFalse(gifts.claimGift(alice.getId(), groupGift.getId()).isOk());
        ServiceResult<Gift> secondClaim = gifts.claimGift(bob.getId(), groupGift.getId());
        assertTrue(secondClaim.isOk());
        assertEquals("claimed", secondClaim.getValue().getStatusKey());

        // Third user exceeds capacity (2)
        assertFalse(gifts.claimGift(charlie.getId(), groupGift.getId()).isOk());

        // Non-creator shared member cannot delete owner's gift
        assertFalse(gifts.deleteGift(bob.getId(), groupGift.getId()).isOk());

        // Bob releases his claim, then creator/owner deletes gift
        assertTrue(gifts.releaseGift(bob.getId(), groupGift.getId()).isOk());
        assertTrue(gifts.deleteGift(alice.getId(), groupGift.getId()).isOk());
    }
}
