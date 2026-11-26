package dev.smartgiftlist.model;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Canonical allowlist for wishlist cover images and user profile avatars.
 */
public final class IconCatalog {

    public static final String DEFAULT_AVATAR_KEY = "Lumibella";
    public static final String DEFAULT_COVER_PATH = "img/anlaesse/PaperList.webp";

    public static final class IconOption {
        private final String key;
        private final String relativePath;
        private final String label;

        public IconOption(String key, String relativePath, String label) {
            this.key = key;
            this.relativePath = relativePath;
            this.label = label;
        }

        public String getKey() {
            return key;
        }

        public String getRelativePath() {
            return relativePath;
        }

        public String getLabel() {
            return label;
        }
    }

    private static final List<IconOption> COVER_OPTIONS = List.of(
            new IconOption("PaperList", "img/anlaesse/PaperList.webp", "PaperList"),
            new IconOption("BronzeList", "img/anlaesse/BrozeList.webp", "BronzeList"),
            new IconOption("SilverList", "img/anlaesse/SilverList.webp", "SilverList"),
            new IconOption("GoldenList", "img/anlaesse/GoldenList.webp", "GoldenList")
    );

    private static final List<IconOption> AVATAR_OPTIONS = List.of(
            new IconOption("Lumibella", "img/anlaesse/Lumibella.webp", "Lumibella"),
            new IconOption("Novaterra", "img/anlaesse/Novaterra.webp", "Novaterra"),
            new IconOption("Quixart", "img/anlaesse/Quixart.webp", "Quixart")
    );

    private static final Map<String, String> COVER_ALIASES = Map.ofEntries(
            Map.entry("paperlist", "img/anlaesse/PaperList.webp"),
            Map.entry("img/anlaesse/paperlist.webp", "img/anlaesse/PaperList.webp"),
            Map.entry("bronzelist", "img/anlaesse/BrozeList.webp"),
            Map.entry("brozelist", "img/anlaesse/BrozeList.webp"),
            Map.entry("img/anlaesse/brozelist.webp", "img/anlaesse/BrozeList.webp"),
            Map.entry("silverlist", "img/anlaesse/SilverList.webp"),
            Map.entry("img/anlaesse/silverlist.webp", "img/anlaesse/SilverList.webp"),
            Map.entry("goldenlist", "img/anlaesse/GoldenList.webp"),
            Map.entry("img/anlaesse/goldenlist.webp", "img/anlaesse/GoldenList.webp")
    );

    private static final Map<String, String> AVATAR_ALIASES = Map.of(
            "lumibella", "Lumibella",
            "novaterra", "Novaterra",
            "quixart", "Quixart"
    );

    private IconCatalog() {
    }

    public static List<IconOption> getCoverOptions() {
        return COVER_OPTIONS;
    }

    public static List<IconOption> getAvatarOptions() {
        return AVATAR_OPTIONS;
    }

    public static String normalizeCoverPath(String rawCover) {
        if (rawCover == null || rawCover.isBlank()) {
            return DEFAULT_COVER_PATH;
        }
        String cleaned = stripRelativePrefix(rawCover.trim()).toLowerCase(Locale.ROOT);
        String direct = COVER_ALIASES.get(cleaned);
        if (direct != null) {
            return direct;
        }
        String fileStem = extractStem(cleaned);
        return COVER_ALIASES.getOrDefault(fileStem, DEFAULT_COVER_PATH);
    }

    public static String normalizeAvatarKey(String rawAvatar) {
        if (rawAvatar == null || rawAvatar.isBlank()) {
            return DEFAULT_AVATAR_KEY;
        }
        String cleaned = stripRelativePrefix(rawAvatar.trim()).toLowerCase(Locale.ROOT);
        String stem = extractStem(cleaned);
        return AVATAR_ALIASES.getOrDefault(stem, DEFAULT_AVATAR_KEY);
    }

    public static String avatarPathForKey(String avatarKey) {
        String normalized = normalizeAvatarKey(avatarKey);
        return "img/anlaesse/" + normalized + ".webp";
    }

    private static String stripRelativePrefix(String value) {
        String result = value;
        while (result.startsWith("../") || result.startsWith("./") || result.startsWith("/")) {
            if (result.startsWith("../")) {
                result = result.substring(3);
            } else if (result.startsWith("./")) {
                result = result.substring(2);
            } else {
                result = result.substring(1);
            }
        }
        return result;
    }

    private static String extractStem(String path) {
        int slash = path.lastIndexOf('/');
        String file = slash >= 0 ? path.substring(slash + 1) : path;
        int dot = file.lastIndexOf('.');
        return dot > 0 ? file.substring(0, dot) : file;
    }
}
