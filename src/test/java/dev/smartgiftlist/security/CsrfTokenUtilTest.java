package dev.smartgiftlist.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsrfTokenUtilTest {

    @Test
    void generatesUniqueHexTokensAndValidatesConstantTime() {
        String tokenA = CsrfTokenUtil.generateToken();
        String tokenB = CsrfTokenUtil.generateToken();

        assertEquals(64, tokenA.length());
        assertNotEquals(tokenA, tokenB);
        assertTrue(CsrfTokenUtil.matches(tokenA, tokenA));
        assertFalse(CsrfTokenUtil.matches(tokenA, tokenB));
        assertFalse(CsrfTokenUtil.matches(tokenA, null));
        assertFalse(CsrfTokenUtil.matches(tokenA, ""));
        assertFalse(CsrfTokenUtil.matches(null, tokenA));
    }
}
