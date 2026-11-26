package dev.smartgiftlist.web;

import java.io.IOException;
import java.util.Optional;

import dev.smartgiftlist.config.ServiceRegistry;
import dev.smartgiftlist.model.FlashMessage;
import dev.smartgiftlist.model.IconCatalog;
import dev.smartgiftlist.model.User;
import dev.smartgiftlist.service.AuthService;
import dev.smartgiftlist.service.ServiceResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Handles profile settings updates and account deletion.
 */
@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        int userId = SessionState.getUserId(session);
        AuthService authService = ServiceRegistry.from(getServletContext()).getAuthService();

        Optional<User> userOpt = authService.getUserById(userId);
        if (userOpt.isEmpty()) {
            if (session != null) {
                session.invalidate();
            }
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        FlashMessage flash = SessionState.popFlash(session);
        if (flash == null) {
            flash = FlashMessage.info(
                    "Profile Settings",
                    "Update your username, password, or avatar below."
            );
        }

        req.setAttribute("profileUser", userOpt.get());
        req.setAttribute("avatarOptions", IconCatalog.getAvatarOptions());
        req.setAttribute("flash", flash);
        req.setAttribute("navPage", "profile");

        req.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        int userId = SessionState.getUserId(session);
        String ctx = req.getContextPath();
        AuthService authService = ServiceRegistry.from(getServletContext()).getAuthService();

        String action = req.getParameter("action");
        if ("save".equals(action)) {
            String password = req.getParameter("password");
            ServiceResult<User> updated = authService.updateProfile(
                    userId,
                    req.getParameter("profilUsername"),
                    password,
                    req.getParameter("confirm_password"),
                    req.getParameter("listIcon")
            );
            if (updated.isOk()) {
                if (password != null && !password.isBlank()) {
                    req.changeSessionId();
                }
                SessionState.setAuthenticatedUser(session, updated.getValue());
            }
            SessionState.setFlash(session, updated.getFlash());
            res.sendRedirect(ctx + "/profile");
            return;
        }

        if ("delete".equals(action)) {
            ServiceResult<Boolean> deleted = authService.deleteAccount(userId);
            if (deleted.isOk()) {
                if (session != null) {
                    session.invalidate();
                }
                HttpSession fresh = req.getSession(true);
                SessionState.ensureCsrfToken(fresh);
                SessionState.setFlash(fresh, deleted.getFlash());
                res.sendRedirect(ctx + "/login");
            } else {
                SessionState.setFlash(session, deleted.getFlash());
                res.sendRedirect(ctx + "/profile");
            }
            return;
        }

        res.sendRedirect(ctx + "/profile");
    }
}
