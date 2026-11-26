package dev.smartgiftlist.web;

import java.io.IOException;

import dev.smartgiftlist.security.CsrfTokenUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ensures every session has a CSRF token and rejects POST requests missing a valid token.
 */
@WebFilter(urlPatterns = {"/login", "/register", "/logout", "/wishlists", "/profile"})
public class CsrfFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(CsrfFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession(true);
        String expectedToken = SessionState.ensureCsrfToken(session);
        req.setAttribute("csrfToken", expectedToken);

        if ("POST".equalsIgnoreCase(req.getMethod())) {
            String providedToken = req.getParameter("_csrf");
            if (!CsrfTokenUtil.matches(expectedToken, providedToken)) {
                LOG.warn("Rejected POST {} due to invalid/missing CSRF token", req.getRequestURI());
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
