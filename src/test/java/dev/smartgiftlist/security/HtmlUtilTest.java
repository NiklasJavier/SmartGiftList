package dev.smartgiftlist.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HtmlUtilTest {

    @Test
    void escapesHtmlSpecialCharactersAndHandlesNulls() {
        assertEquals("", HtmlUtil.escape(null));
        assertEquals("", HtmlUtil.escape(""));
        assertEquals(
                "&lt;script&gt;alert(&quot;x&quot; &amp; &#39;y&#39;)&lt;/script&gt;",
                HtmlUtil.escape("<script>alert(\"x\" & 'y')</script>")
        );
    }

    @Test
    void sanitizesUrlsToAllowOnlyHttpAndHttps() {
        assertEquals("https://example.test/gift?a=1", HtmlUtil.sanitizeHttpUrl("  https://example.test/gift?a=1  "));
        assertEquals("http://localhost:8080/item", HtmlUtil.sanitizeHttpUrl("http://localhost:8080/item"));
        assertEquals("", HtmlUtil.sanitizeHttpUrl("javascript:alert(1)"));
        assertEquals("", HtmlUtil.sanitizeHttpUrl("data:text/html,<script>1</script>"));
        assertEquals("", HtmlUtil.sanitizeHttpUrl("not a url"));
        assertEquals("", HtmlUtil.sanitizeHttpUrl(null));
        assertEquals("", HtmlUtil.sanitizeHttpUrl("   "));
    }

    @Test
    void truncatesCharsAndWordsSafely() {
        assertEquals("Coffee...", HtmlUtil.truncateChars("Coffee Maker Pro", 6));
        assertEquals("Mug", HtmlUtil.truncateChars("Mug", 12));
        assertEquals("one two...", HtmlUtil.truncateWords("one   two three four", 2));
        assertEquals("", HtmlUtil.truncateWords(null, 5));
    }
}
