package dev.smartgiftlist.web;

import java.io.IOException;

import dev.smartgiftlist.config.ServiceRegistry;
import dev.smartgiftlist.model.FlashMessage;
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
 * Handles login, registration, and logout workflows using Post/Redirect/Get.
 */
@WebServlet(urlPatterns = {"/login", "/register", "/logout"})
public class AuthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String path = req.getServletPath();
        HttpSession session = req.getSession(true);
        FlashMessage flash = SessionState.popFlash(session);

        switch (path) {
            case "/register" -> {
                if (flash == null) {
                    flash = FlashMessage.info(
                            "Welcome to SmartGiftList.",
                            "Complete the registration form below to create a new account."
                    );
                }
                SessionState.populateAndClearRegistrationDraft(req, session);
                req.setAttribute("flash", flash);
                req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, res);
            }
            case "/login" -> {
                if (flash == null) {
                    flash = FlashMessage.info(
                            "Welcome to SmartGiftList.",
                            "Please sign in with your credentials to access your account."
                    );
                }
                req.setAttribute("flash", flash);
                req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, res);
            }
            case "/logout" -> res.sendRedirect(req.getContextPath() + "/login");
            default -> res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        String path = req.getServletPath();
        String ctx = req.getContextPath();
        AuthService authService = ServiceRegistry.from(getServletContext()).getAuthService();

        switch (path) {
            case "/login" -> handleLogin(req, res, ctx, authService);
            case "/register" -> handleRegister(req, res, ctx, authService);
            case "/logout" -> handleLogout(req, res, ctx);
            default -> res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private static void handleLogin(
            HttpServletRequest req,
            HttpServletResponse res,
            String ctx,
            AuthService authService
    ) throws IOException {
        ServiceResult<User> result = authService.authenticate(
                req.getParameter("username"),
                req.getParameter("password")
        );
        HttpSession session = req.getSession(true);
        if (result.isOk()) {
            req.changeSessionId();
            SessionState.setAuthenticatedUser(session, result.getValue());
            SessionState.setActiveNetId(session, null);
            SessionState.setFlash(session, result.getFlash());
            res.sendRedirect(ctx + "/wishlists");
        } else {
            SessionState.setFlash(session, result.getFlash());
            res.sendRedirect(ctx + "/login");
        }
    }

    private static void handleRegister(
            HttpServletRequest req,
            HttpServletResponse res,
            String ctx,
            AuthService authService
    ) throws IOException {
        String username = req.getParameter("username");
        String email = req.getParameter("email");
        String birthday = req.getParameter("birthday");

        ServiceResult<User> result = authService.register(
                username,
                email,
                birthday,
                req.getParameter("password"),
                req.getParameter("confirm_password")
        );

        HttpSession session = req.getSession(true);
        SessionState.setFlash(session, result.getFlash());
        if (result.isOk()) {
            SessionState.clearRegistrationDraft(session);
            res.sendRedirect(ctx + "/login");
        } else {
            SessionState.saveRegistrationDraft(session, username, email, birthday);
            res.sendRedirect(ctx + "/register");
        }
    }

    private static void handleLogout(HttpServletRequest req, HttpServletResponse res, String ctx)
            throws IOException {
        HttpSession existing = req.getSession(false);
        if (existing != null) {
            existing.invalidate();
        }
        HttpSession fresh = req.getSession(true);
        SessionState.ensureCsrfToken(fresh);
        SessionState.setFlash(
                fresh,
                FlashMessage.info("You have been signed out.", "Sign in again at any time.")
        );
        res.sendRedirect(ctx + "/login");
    }
}
