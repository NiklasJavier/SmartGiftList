package dev.smartgiftlist.beans;

import java.sql.Date;

import dev.smartgiftlist.jdbc.NoConnectionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DomainAndMessageBeanTest {

    @Test
    void giftFormatsTypeLabelsCleanly() {
        Gift empty = new Gift();
        assertEquals("", empty.getGiftID());
        assertEquals("", empty.getWishlistType());

        Gift single = new Gift(1, 2, "Book", "Hardcover", "https://example.com", "PaperList", 25.0, 1, false);
        assertEquals("Individual Gift", single.getWishlistType());

        Gift surpriseGroup = new Gift(2, 2, "Trip", "Weekend", "", "GoldenList", 200.0, 4, true);
        assertEquals("Surprise Gift, Group Gift", surpriseGroup.getWishlistType());
    }

    @Test
    void accountBeanValidatesDatesAndPasswordConfirmationWithoutDb() throws NoConnectionException {
        AccountBean account = new AccountBean(false);
        account.setPassword("secret123");
        account.setConfirm_password("secret123");
        assertTrue(account.comparePasswords());

        account.setConfirm_password("mismatch");
        assertFalse(account.comparePasswords());

        account.setStringToSqlDate("1999-05-14");
        assertEquals(Date.valueOf("1999-05-14"), account.getBirthday());

        account.setStringToSqlDate("not-a-date");
        assertNull(account.getBirthday());
    }

    @Test
    void messageBeanRendersEscapedEnglishMessages() {
        MessageBean msg = new MessageBean();
        assertTrue(msg.getMessageHtml().contains("Welcome to SmartGiftList."));

        msg.setRegisterSuccessful("<alice>");
        assertTrue(msg.getMessageHtml().contains("Welcome, &lt;alice&gt;."));
        assertFalse(msg.getMessageHtml().contains("<alice>"));
    }
}
