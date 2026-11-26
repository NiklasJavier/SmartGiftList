package dev.smartgiftlist.web;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Serves the public SmartGiftList landing view for / and /landing.
 */
@WebServlet(urlPatterns = {"", "/landing"})
public class LandingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.setAttribute("authenticated", SessionState.isAuthenticated(req.getSession(false)));
        req.getRequestDispatcher("/WEB-INF/views/landing.jsp").forward(req, res);
    }
}
