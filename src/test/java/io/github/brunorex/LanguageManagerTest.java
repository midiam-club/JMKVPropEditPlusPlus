package io.github.brunorex;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.Locale;

/**
 * Unit tests for LanguageManager class.
 */
public class LanguageManagerTest {

    @Before
    public void setUp() {
        // Reset to default locale before each test
        LanguageManager.setLocale(Locale.ENGLISH);
    }

    @Test
    public void testSetLocale_English() {
        LanguageManager.setLocale(Locale.ENGLISH);
        assertEquals(Locale.ENGLISH, LanguageManager.getLocale());
    }

    @Test
    public void testSetLocale_Spanish() {
        LanguageManager.setLocale(new Locale.Builder().setLanguage("es").build());
        assertEquals(new Locale.Builder().setLanguage("es").build(), LanguageManager.getLocale());
    }

    @Test
    public void testGetString_existingKey() {
        LanguageManager.setLocale(Locale.ENGLISH);
        String result = LanguageManager.getString("tab.general");
        assertEquals("General", result);
    }

    @Test
    public void testGetString_nonExistingKey() {
        String result = LanguageManager.getString("nonexistent.key.xyz");
        assertTrue(result.startsWith("!") && result.endsWith("!"));
    }

    @Test
    public void testGetString_spanishTranslation() {
        LanguageManager.setLocale(new Locale.Builder().setLanguage("es").build());
        String result = LanguageManager.getString("button.cancel");
        assertEquals("Cancelar", result);
    }

    @Test
    public void testGetString_fallbackToDefault() {
        // Set an unsupported locale
        LanguageManager.setLocale(new Locale.Builder().setLanguage("xx").build()); // Non-existent
        // Should fallback to English
        String result = LanguageManager.getString("tab.general");
        assertNotNull(result);
        assertFalse(result.startsWith("!"));
    }
}
