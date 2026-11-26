package dev.smartgiftlist.security;

import java.net.URI;
import java.util.Arrays;

/**
 * HTML escaping, safe HTTP URL sanitization, and display truncation helpers.
 */
public final class HtmlUtil {

    private HtmlUtil() {
    }

    public static String escape(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder(input.length() + 16);
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append("&quot;");
                case '\'' -> out.append("&#39;");
                default -> out.append(c);
            }
        }
        return out.toString();
    }

    /**
     * Allows only http/https URLs (or blank strings) and rejects script/data URIs.
     */
    public static String sanitizeHttpUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return "";
        }
        String trimmed = rawUrl.trim();
        try {
            URI uri = URI.create(trimmed);
            String scheme = uri.getScheme();
            if (scheme == null) {
                return "";
            }
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                return "";
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                return "";
            }
            return uri.toASCIIString();
        } catch (IllegalArgumentException ex) {
            return "";
        }
    }

    public static String truncateChars(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (maxLength <= 0 || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    public static String truncateWords(String text, int wordLimit) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String[] words = text.trim().split("\\s+", wordLimit + 1);
        if (words.length > wordLimit) {
            return String.join(" ", Arrays.copyOf(words, wordLimit)) + "...";
        }
        return text.trim();
    }
}
