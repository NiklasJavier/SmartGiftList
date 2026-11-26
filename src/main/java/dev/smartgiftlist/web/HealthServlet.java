package dev.smartgiftlist.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import dev.smartgiftlist.config.ServiceRegistry;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Live readiness probe verifying Tomcat deployment and PostgreSQL pool connectivity.
 */
@WebServlet("/health")
public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json; charset=UTF-8");
        try {
            ServiceRegistry registry = ServiceRegistry.from(getServletContext());
            try (Connection conn = registry.getDataSource().getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("SELECT 1");
            }
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write("{\"status\":\"ok\"}");
        } catch (RuntimeException | SQLException ex) {
            res.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            res.getWriter().write("{\"status\":\"error\"}");
        }
    }
}
