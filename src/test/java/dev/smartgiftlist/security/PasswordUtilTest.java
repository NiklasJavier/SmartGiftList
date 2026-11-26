package dev.smartgiftlist.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void hashesWithUniqueRandomSaltsAndVerifiesCorrectPassword() {
        String first = PasswordUtil.hashPassword("secret123");
        String second = PasswordUtil.hashPassword("secret123");

        assertNotEquals(first, second);
        assertTrue(first.startsWith("pbkdf2$120000$"));
        assertTrue(PasswordUtil.verifyPassword("secret123", first));
        assertTrue(PasswordUtil.verifyPassword("secret123", second));
        assertFalse(PasswordUtil.verifyPassword("wrong-password", first));
    }

    @Test
    void rejectsNullBlankOrMalformedInputsGracefully() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword(null));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword("   "));

        assertFalse(PasswordUtil.verifyPassword(null, "pbkdf2$120000$aa$bb"));
        assertFalse(PasswordUtil.verifyPassword("secret", null));
        assertFalse(PasswordUtil.verifyPassword("secret", ""));
        assertFalse(PasswordUtil.verifyPassword("secret", "plaintext"));
        assertFalse(PasswordUtil.verifyPassword("secret", "pbkdf2$0$aa$bb"));
        assertFalse(PasswordUtil.verifyPassword("secret", "pbkdf2$100$nothex$bb"));
    }
}
