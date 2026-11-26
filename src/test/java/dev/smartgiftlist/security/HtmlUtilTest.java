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
}
