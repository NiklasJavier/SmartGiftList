package dev.smartgiftlist.repository;

import java.util.List;

import dev.smartgiftlist.model.ChatComment;

/**
 * Persistence port for wishlist discussion comments.
 */
public interface CommentRepository {

    List<ChatComment> findByWishlistId(int wishlistId);

    void addUserComment(int wishlistId, int userId, String commentText);

    void addEventComment(int wishlistId, String commentText);

    boolean withdrawLastUserComment(int wishlistId, int userId, String withdrawnNotice);
}
