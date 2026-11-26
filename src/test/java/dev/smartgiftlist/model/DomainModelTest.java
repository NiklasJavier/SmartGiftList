package dev.smartgiftlist.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DomainModelTest {

    @Test
    void giftDerivesStatusesBadgesAndFormattedFieldsCleanly() {
        Gift individual = new Gift(1, 10, "Mug", "Ceramic coffee mug", "", "PaperList",
                new BigDecimal("19.99"), 1, false, 7, 0, List.of());
        assertEquals("single", individual.getStatusKey());
        assertEquals("IG", individual.getTypeBadges());
        assertEquals("Individual Gift", individual.getDetailTypeLabel());
        assertEquals("19.99", individual.getFormattedPrice());

        Gift group = new Gift(2, 10, "Espresso Machine", "Shared kitchen gift", "", "GoldenList",
                new BigDecimal("200.00"), 2, false, 7, 1, List.of("Lumibella"));
        assertEquals("group", group.getStatusKey());
        assertEquals("GG", group.getTypeBadges());
        assertEquals("Group Gift", group.getDetailTypeLabel());
        assertEquals("200", group.getFormattedPrice());
        assertEquals(List.of("img/anlaesse/Lumibella.webp"), group.getClaimerAvatarPaths());

        Gift claimedSurprise = new Gift(3, 10, "Watch", "Surprise gift", "", "SilverList",
                BigDecimal.ZERO, 2, true, 7, 2, List.of("Lumibella", "Novaterra"));
        assertEquals("claimed", claimedSurprise.getStatusKey());
        assertEquals("SG, CL", claimedSurprise.getTypeBadges());
        assertEquals("Surprise Gift, Group Gift", claimedSurprise.getDetailTypeLabel());
        assertEquals("", claimedSurprise.getFormattedPrice());
    }

    @Test
    void iconCatalogNormalizesLegacyAndUntrustedPaths() {
        assertEquals("img/anlaesse/BrozeList.webp", IconCatalog.normalizeCoverPath("../img/anlaesse/BrozeList.webp"));
        assertEquals("img/anlaesse/GoldenList.webp", IconCatalog.normalizeCoverPath("GoldenList"));
        assertEquals("img/anlaesse/PaperList.webp", IconCatalog.normalizeCoverPath("../../etc/passwd"));

        assertEquals("Novaterra", IconCatalog.normalizeAvatarKey("../img/anlaesse/Novaterra.webp"));
        assertEquals("Lumibella", IconCatalog.normalizeAvatarKey("malicious.png"));
    }

    @Test
    void userAndWishlistFormatIsoDatesSafely() {
        User user = new User(1, "alice", "a@test.example", "hash",
                LocalDate.of(1999, 5, 14), false, "Quixart", LocalDateTime.now());
        assertEquals("1999-05-14", user.getBirthdayIso());
        assertEquals("img/anlaesse/Quixart.webp", user.getAvatarPath());

        Wishlist wishlist = new Wishlist(2, "SGL-ABCD-1234", "Birthday", "Party",
                null, "one two three", "", "SilverList", 1, LocalDateTime.now());
        assertEquals("", wishlist.getEventDateIso());
        assertEquals("img/anlaesse/SilverList.webp", wishlist.getIcon());
    }
}
