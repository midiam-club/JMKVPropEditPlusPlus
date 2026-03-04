package io.github.brunorex;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for ErrorCode enum.
 */
public class ErrorCodeTest {

    @Test
    public void testGetMessageKey() {
        assertEquals("error.file.not.found", ErrorCode.FILE_NOT_FOUND.getMessageKey());
        assertEquals("error.mkvpropedit.not.found", ErrorCode.MKVPROPEDIT_NOT_FOUND.getMessageKey());
    }

    @Test
    public void testGetMessage_returnsNonEmpty() {
        for (ErrorCode code : ErrorCode.values()) {
            String msg = code.getMessage();
            assertNotNull(msg);
            assertFalse(msg.isEmpty());
        }
    }

    @Test
    public void testAllCodesHaveI18nKey() {
        for (ErrorCode code : ErrorCode.values()) {
            String key = code.getMessageKey();
            assertNotNull(key);
            assertTrue(key.startsWith("error."));
        }
    }
}
