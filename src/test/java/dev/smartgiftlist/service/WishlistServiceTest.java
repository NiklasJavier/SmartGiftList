package dev.smartgiftlist.service;

import dev.smartgiftlist.model.DashboardData;
import dev.smartgiftlist.model.User;
import dev.smartgiftlist.model.Wishlist;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WishlistServiceTest {

    @Test
    void createsUpdatesConnectsAndEnforcesOwnershipBoundaries() {
        InMemoryRepositories repo = new InMemoryRepositories();
        AuthService auth = new AuthService(repo.users());
        WishlistService wishlists = new WishlistService(repo.wishlists(), repo.gifts(), repo.comments());

        User alice = auth.register("alice", "a@test.example", "", "secret123", "secret123").getValue();
        User bob = auth.register("bob", "b@test.example", "", "secret123", "secret123").getValue();

        ServiceResult<Wishlist> created = wishlists.createWishlist(
                alice.getId(), "Birthday", "Party", "", "My wishlist", "javascript:alert(1)", "GoldenList"
        );
        assertTrue(created.isOk());
        Wishlist list = created.getValue();
        assertTrue(list.getNetId().startsWith("SGL-"));
        assertEquals("", list.getUrl());
        assertNull(list.getEventDate());

        // Bob cannot view or modify Alice's private list before connecting
        DashboardData bobBefore = wishlists.loadDashboardData(bob.getId(), list.getNetId(), 0);
        assertNull(bobBefore.getActiveWishlist());
        assertFalse(wishlists.updateWishlistDetails(bob.getId(), list.getId(), "", "Hack", "", "PaperList", "").isOk());

        // Alice cannot connect to her own wishlist
        assertFalse(wishlists.connectByNetId(alice.getId(), list.getNetId()).isOk());

        // Bob connects via NetID and still cannot overwrite Alice's list details
        assertTrue(wishlists.connectByNetId(bob.getId(), list.getNetId()).isOk());
        assertFalse(wishlists.updateWishlistDetails(bob.getId(), list.getId(), "", "Hack", "", "PaperList", "").isOk());

        // Bob leaving shared wishlist does not delete Alice's wishlist
        assertTrue(wishlists.deleteOrLeaveWishlist(bob.getId(), list.getId()).isOk());
        assertTrue(repo.wishlists().findById(list.getId()).isPresent());

        // Owner delete removes wishlist
        assertTrue(wishlists.deleteOrLeaveWishlist(alice.getId(), list.getId()).isOk());
        assertTrue(repo.wishlists().findById(list.getId()).isEmpty());
    }
}
