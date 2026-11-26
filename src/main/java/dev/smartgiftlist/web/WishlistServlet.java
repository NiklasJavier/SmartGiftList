package dev.smartgiftlist.web;

import java.io.IOException;
import java.util.Optional;

import dev.smartgiftlist.config.ServiceRegistry;
import dev.smartgiftlist.model.DashboardData;
import dev.smartgiftlist.model.FlashMessage;
import dev.smartgiftlist.model.Gift;
import dev.smartgiftlist.model.IconCatalog;
import dev.smartgiftlist.model.Wishlist;
import dev.smartgiftlist.service.CommentService;
import dev.smartgiftlist.service.GiftService;
import dev.smartgiftlist.service.ServiceResult;
import dev.smartgiftlist.service.WishlistService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Controller for authenticated wishlist dashboard, gift coordination, and discussion actions.
 */
@WebServlet("/wishlists")
public class WishlistServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        int userId = SessionState.getUserId(session);
        WishlistService wishlistService = ServiceRegistry.from(getServletContext()).getWishlistService();

        String queryNetId = req.getParameter("netId");
        String preferredNetId = (queryNetId != null && !queryNetId.isBlank())
                ? queryNetId.trim()
                : SessionState.getActiveNetId(session);
        int selectedGiftId = parseIntOrZero(req.getParameter("giftId"));
        boolean openDetails = "1".equals(req.getParameter("openDetails"));

        DashboardData dashboard = wishlistService.loadDashboardData(userId, preferredNetId, selectedGiftId);
        if (dashboard.getActiveWishlist() != null) {
            SessionState.setActiveNetId(session, dashboard.getActiveWishlist().getNetId());
        } else {
            SessionState.setActiveNetId(session, null);
        }

        FlashMessage flash = SessionState.popFlash(session);
        if (flash == null) {
            flash = FlashMessage.banner("Create and manage your personal and shared wishlists here.");
        }

        req.setAttribute("dashboard", dashboard);
        req.setAttribute("wishlist", dashboard.getActiveWishlist());
        req.setAttribute("isOwner", dashboard.isActiveWishlistOwner());
        req.setAttribute("coverOptions", IconCatalog.getCoverOptions());
        req.setAttribute("selectedGift", dashboard.getSelectedGift());
        req.setAttribute("openGiftDetailsModal", openDetails && dashboard.getSelectedGift() != null);
        req.setAttribute("flash", flash);
        req.setAttribute("navPage", "wishlists");

        req.getRequestDispatcher("/WEB-INF/views/wishlists.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        int userId = SessionState.getUserId(session);
        String ctx = req.getContextPath();
        ServiceRegistry services = ServiceRegistry.from(getServletContext());
        WishlistService wishlistService = services.getWishlistService();
        GiftService giftService = services.getGiftService();
        CommentService commentService = services.getCommentService();

        String action = parameterOrEmpty(req, "action");
        String redirectTarget = ctx + "/wishlists";

        switch (action) {
            case "create-wishlist" -> {
                ServiceResult<Wishlist> created = wishlistService.createWishlist(
                        userId,
                        req.getParameter("wishlist_name"),
                        req.getParameter("occasion"),
                        req.getParameter("date"),
                        req.getParameter("description"),
                        req.getParameter("links"),
                        req.getParameter("list_icon")
                );
                if (created.isOk()) {
                    SessionState.setActiveNetId(session, created.getValue().getNetId());
                }
                SessionState.setFlash(session, created.getFlash());
            }
            case "select-wishlist" -> {
                String netId = parameterOrEmpty(req, "netId");
                Optional<Wishlist> target = wishlistService.findAccessibleByNetId(userId, netId);
                if (target.isPresent()) {
                    SessionState.setActiveNetId(session, target.get().getNetId());
                    SessionState.setFlash(session, FlashMessage.banner("Wishlist selected."));
                } else {
                    SessionState.setFlash(session, FlashMessage.error(
                            "Wishlist unavailable.",
                            "The requested wishlist could not be opened."
                    ));
                }
            }
            case "update-wishlist" -> {
                int wishlistId = resolveActiveWishlistId(req, session, userId, wishlistService);
                ServiceResult<Wishlist> updated = wishlistService.updateWishlistDetails(
                        userId,
                        wishlistId,
                        req.getParameter("links"),
                        req.getParameter("occasion"),
                        req.getParameter("description"),
                        req.getParameter("coverIcon"),
                        req.getParameter("eventDate")
                );
                SessionState.setFlash(session, updated.getFlash());
            }
            case "delete-wishlist" -> {
                int wishlistId = resolveActiveWishlistId(req, session, userId, wishlistService);
                ServiceResult<Boolean> removed = wishlistService.deleteOrLeaveWishlist(userId, wishlistId);
                if (removed.isOk()) {
                    SessionState.setActiveNetId(session, null);
                }
                SessionState.setFlash(session, removed.getFlash());
            }
            case "connect-wishlist" -> {
                ServiceResult<Wishlist> connected = wishlistService.connectByNetId(
                        userId,
                        req.getParameter("netIdConnect")
                );
                if (connected.isOk()) {
                    SessionState.setActiveNetId(session, connected.getValue().getNetId());
                }
                SessionState.setFlash(session, connected.getFlash());
            }
            case "add-gift" -> {
                int wishlistId = resolveActiveWishlistId(req, session, userId, wishlistService);
                String giftType = parameterOrEmpty(req, "giftType");
                String peopleParam = "single".equals(giftType)
                        ? "1"
                        : req.getParameter("numberOfPeople");
                ServiceResult<Gift> added = giftService.addGift(
                        userId,
                        wishlistId,
                        req.getParameter("giftName"),
                        req.getParameter("description"),
                        req.getParameter("link"),
                        req.getParameter("giftIcon"),
                        req.getParameter("price"),
                        peopleParam,
                        req.getParameter("surpriseGift") != null
                );
                SessionState.setFlash(session, added.getFlash());
            }
            case "gift-details" -> {
                int giftId = parseIntOrZero(req.getParameter("selectedGiftId"));
                Optional<Gift> accessible = giftService.findAccessibleGift(userId, giftId);
                if (accessible.isPresent()) {
                    SessionState.setFlash(session, FlashMessage.banner("Gift selected."));
                    redirectTarget = ctx + "/wishlists?openDetails=1&giftId=" + giftId;
                } else {
                    SessionState.setFlash(session, FlashMessage.banner("No gift was selected."));
                }
            }
            case "delete-gift" -> {
                int giftId = parseIntOrZero(req.getParameter("selectedGiftId"));
                ServiceResult<Boolean> deleted = giftService.deleteGift(userId, giftId);
                SessionState.setFlash(session, deleted.getFlash());
            }
            case "claim-gift" -> {
                int giftId = parseIntOrZero(req.getParameter("giftId"));
                ServiceResult<Gift> claimed = giftService.claimGift(userId, giftId);
                SessionState.setFlash(session, claimed.getFlash());
                if (giftService.findAccessibleGift(userId, giftId).isPresent()) {
                    redirectTarget = ctx + "/wishlists?openDetails=1&giftId=" + giftId;
                }
            }
            case "release-gift" -> {
                int giftId = parseIntOrZero(req.getParameter("giftId"));
                ServiceResult<Gift> released = giftService.releaseGift(userId, giftId);
                SessionState.setFlash(session, released.getFlash());
                if (giftService.findAccessibleGift(userId, giftId).isPresent()) {
                    redirectTarget = ctx + "/wishlists?openDetails=1&giftId=" + giftId;
                }
            }
            case "send-comment" -> {
                int wishlistId = resolveActiveWishlistId(req, session, userId, wishlistService);
                ServiceResult<Boolean> sent = commentService.addComment(
                        userId, wishlistId, req.getParameter("commentText")
                );
                if (!sent.isOk()) {
                    SessionState.setFlash(session, sent.getFlash());
                }
            }
            case "withdraw-comment" -> {
                int wishlistId = resolveActiveWishlistId(req, session, userId, wishlistService);
                ServiceResult<Boolean> withdrawn = commentService.withdrawLastComment(userId, wishlistId);
                if (!withdrawn.isOk()) {
                    SessionState.setFlash(session, withdrawn.getFlash());
                }
            }
            case "refresh-comments" -> {
                // Clean PRG reload.
            }
            default -> {
                // Fallback redirect cleanly to dashboard.
            }
        }

        res.sendRedirect(redirectTarget);
    }

    private static int resolveActiveWishlistId(
            HttpServletRequest req,
            HttpSession session,
            int userId,
            WishlistService wishlistService
    ) {
        int explicitId = parseIntOrZero(req.getParameter("wishlistId"));
        if (explicitId > 0) {
            return explicitId;
        }
        String netId = SessionState.getActiveNetId(session);
        return wishlistService.findAccessibleByNetId(userId, netId)
                .map(Wishlist::getId)
                .orElse(0);
    }

    private static int parseIntOrZero(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static String parameterOrEmpty(HttpServletRequest req, String name) {
        String val = req.getParameter(name);
        return val != null ? val.trim() : "";
    }
}
