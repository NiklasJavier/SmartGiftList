package dev.smartgiftlist.service;

import java.util.List;

import dev.smartgiftlist.model.ChatComment;
import dev.smartgiftlist.model.CommentType;
import dev.smartgiftlist.model.User;
import dev.smartgiftlist.model.Wishlist;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommentServiceTest {

    @Test
    void enforcesMembershipAndWithdrawsMostRecentCommentCleanly() {
        InMemoryRepositories repo = new InMemoryRepositories();
        AuthService auth = new AuthService(repo.users());
        WishlistService wishlists = new WishlistService(repo.wishlists(), repo.gifts(), repo.comments());
        CommentService comments = new CommentService(repo.comments(), repo.wishlists());

        User alice = auth.register("alice", "a@test.example", "", "secret123", "secret123").getValue();
        User outsider = auth.register("outsider", "o@test.example", "", "secret123", "secret123").getValue();
        Wishlist list = wishlists.createWishlist(alice.getId(), "Wedding", "", "", "", "", "SilverList").getValue();

        assertFalse(comments.addComment(outsider.getId(), list.getId(), "Unauthorized").isOk());
        assertFalse(comments.addComment(alice.getId(), list.getId(), "   ").isOk());

        assertTrue(comments.addComment(alice.getId(), list.getId(), "Bringing plates").isOk());
        assertTrue(comments.addComment(alice.getId(), list.getId(), "Actually cups").isOk());

        assertTrue(comments.withdrawLastComment(alice.getId(), list.getId()).isOk());
        List<ChatComment> thread = repo.comments().findByWishlistId(list.getId());
        assertEquals(2, thread.size());
        assertEquals(CommentType.EVENT, thread.get(0).getType());
        assertEquals(CommentService.WITHDRAWN_NOTICE, thread.get(0).getText());
        assertEquals("Bringing plates", thread.get(1).getText());
    }
}
