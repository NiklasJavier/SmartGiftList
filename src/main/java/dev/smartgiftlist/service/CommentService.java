package dev.smartgiftlist.service;

import dev.smartgiftlist.model.FlashMessage;
import dev.smartgiftlist.repository.CommentRepository;
import dev.smartgiftlist.repository.WishlistRepository;

/**
 * Wishlist discussion comment service with membership authorization.
 */
public class CommentService {

    static final String WITHDRAWN_NOTICE = "This message was withdrawn by the participant.";
    private static final int MAX_COMMENT_LENGTH = 2000;

    private final CommentRepository commentRepository;
    private final WishlistRepository wishlistRepository;

    public CommentService(CommentRepository commentRepository, WishlistRepository wishlistRepository) {
        this.commentRepository = commentRepository;
        this.wishlistRepository = wishlistRepository;
    }

    public ServiceResult<Boolean> addComment(int userId, int wishlistId, String rawCommentText) {
        if (wishlistId <= 0 || !wishlistRepository.isAccessibleByUser(wishlistId, userId)) {
            return ServiceResult.failure(FlashMessage.error(
                    "Wishlist unavailable.",
                    "You do not have access to post on this wishlist."
            ));
        }

        String text = rawCommentText != null ? rawCommentText.trim() : "";
        if (text.isEmpty()) {
            return ServiceResult.failure(FlashMessage.banner("Please enter a comment before sending."));
        }
        if (text.length() > MAX_COMMENT_LENGTH) {
            text = text.substring(0, MAX_COMMENT_LENGTH);
        }

        commentRepository.addUserComment(wishlistId, userId, text);
        return ServiceResult.success(true, null);
    }

    public ServiceResult<Boolean> withdrawLastComment(int userId, int wishlistId) {
        if (wishlistId <= 0 || !wishlistRepository.isAccessibleByUser(wishlistId, userId)) {
            return ServiceResult.failure(FlashMessage.error(
                    "Wishlist unavailable.",
                    "You do not have access to modify this discussion."
            ));
        }
        boolean updated = commentRepository.withdrawLastUserComment(wishlistId, userId, WITHDRAWN_NOTICE);
        return ServiceResult.success(updated, null);
    }
}
