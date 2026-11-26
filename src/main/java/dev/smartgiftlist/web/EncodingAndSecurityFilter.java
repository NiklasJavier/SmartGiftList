package dev.smartgiftlist.web;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Forces UTF-8 encoding and sets baseline browser security and cache headers.
 */
@WebFilter("/*")
public class EncodingAndSecurityFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        if (request instanceof HttpServletRequest httpReq && response instanceof HttpServletResponse httpRes) {
            httpRes.setHeader("X-Content-Type-Options", "nosniff");
            httpRes.setHeader("X-Frame-Options", "DENY");
            httpRes.setHeader("Referrer-Policy", "same-origin");

            String uri = httpReq.getRequestURI().substring(httpReq.getContextPath().length());
            if (!uri.startsWith("/css/") && !uri.startsWith("/js/") && !uri.startsWith("/img/")) {
                httpRes.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
                httpRes.setHeader("Pragma", "no-cache");
            }
        }

        chain.doFilter(request, response);
    }
}
