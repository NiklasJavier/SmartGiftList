package dev.smartgiftlist.web;

import dev.smartgiftlist.model.FlashMessage;
import dev.smartgiftlist.model.User;
import dev.smartgiftlist.security.CsrfTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Typed accessor for minimal scalar and flash attributes stored in {@link HttpSession}.
 */
public final class SessionState {

    private static final String KEY_USER_ID = "sgl.userId";
    private static final String KEY_USERNAME = "sgl.username";
    private static final String KEY_ACTIVE_NET_ID = "sgl.activeNetId";
    private static final String KEY_FLASH = "sgl.flash";
    private static final String KEY_CSRF = "sgl.csrfToken";
    private static final String KEY_REG_USERNAME = "sgl.regDraftUsername";
    private static final String KEY_REG_EMAIL = "sgl.regDraftEmail";
    private static final String KEY_REG_BIRTHDAY = "sgl.regDraftBirthday";

    private SessionState() {
    }

    public static boolean isAuthenticated(HttpSession session) {
        return getUserId(session) > 0;
    }

    public static int getUserId(HttpSession session) {
        if (session == null) {
            return 0;
        }
        Object raw = session.getAttribute(KEY_USER_ID);
        return raw instanceof Integer id ? id : 0;
    }

    public static String getUsername(HttpSession session) {
        if (session == null) {
            return "";
        }
        Object raw = session.getAttribute(KEY_USERNAME);
        return raw instanceof String name ? name : "";
    }

    public static void setAuthenticatedUser(HttpSession session, User user) {
        session.setAttribute(KEY_USER_ID, user.getId());
        session.setAttribute(KEY_USERNAME, user.getUsername());
    }

    public static String getActiveNetId(HttpSession session) {
        if (session == null) {
            return "";
        }
        Object raw = session.getAttribute(KEY_ACTIVE_NET_ID);
        return raw instanceof String netId ? netId : "";
    }

    public static void setActiveNetId(HttpSession session, String netId) {
        if (netId == null || netId.isBlank()) {
            session.removeAttribute(KEY_ACTIVE_NET_ID);
        } else {
            session.setAttribute(KEY_ACTIVE_NET_ID, netId.trim());
        }
    }

    public static void setFlash(HttpSession session, FlashMessage flash) {
        if (flash != null) {
            session.setAttribute(KEY_FLASH, flash);
        }
    }

    public static FlashMessage popFlash(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object raw = session.getAttribute(KEY_FLASH);
        session.removeAttribute(KEY_FLASH);
        return raw instanceof FlashMessage msg ? msg : null;
    }

    public static String ensureCsrfToken(HttpSession session) {
        Object raw = session.getAttribute(KEY_CSRF);
        if (raw instanceof String existing && !existing.isBlank()) {
            return existing;
        }
        String fresh = CsrfTokenUtil.generateToken();
        session.setAttribute(KEY_CSRF, fresh);
        return fresh;
    }

    public static String getCsrfToken(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object raw = session.getAttribute(KEY_CSRF);
        return raw instanceof String token ? token : null;
    }

    public static void saveRegistrationDraft(HttpSession session, String username, String email, String birthday) {
        session.setAttribute(KEY_REG_USERNAME, username != null ? username.trim() : "");
        session.setAttribute(KEY_REG_EMAIL, email != null ? email.trim() : "");
        session.setAttribute(KEY_REG_BIRTHDAY, birthday != null ? birthday.trim() : "");
    }

    public static void populateAndClearRegistrationDraft(HttpServletRequest request, HttpSession session) {
        if (session == null) {
            return;
        }
        request.setAttribute("draftUsername", stringOrEmpty(session.getAttribute(KEY_REG_USERNAME)));
        request.setAttribute("draftEmail", stringOrEmpty(session.getAttribute(KEY_REG_EMAIL)));
        request.setAttribute("draftBirthday", stringOrEmpty(session.getAttribute(KEY_REG_BIRTHDAY)));
        clearRegistrationDraft(session);
    }

    public static void clearRegistrationDraft(HttpSession session) {
        if (session != null) {
            session.removeAttribute(KEY_REG_USERNAME);
            session.removeAttribute(KEY_REG_EMAIL);
            session.removeAttribute(KEY_REG_BIRTHDAY);
        }
    }

    private static String stringOrEmpty(Object value) {
        return value instanceof String s ? s : "";
    }
}
