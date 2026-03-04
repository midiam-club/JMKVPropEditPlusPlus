package io.github.brunorex;

import org.junit.Test;

import static org.junit.Assert.*;

import java.io.File;

/**
 * Unit tests for Utils class.
 */
public class UtilsTest {

    @Test
    public void testIsWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        boolean isWin = os.contains("win");
        assertEquals(isWin, Utils.isWindows());
    }

    @Test
    public void testIsMac() {
        String os = System.getProperty("os.name").toLowerCase();
        boolean isMac = os.contains("mac");
        assertEquals(isMac, Utils.isMac());
    }

    @Test
    public void testIsLinux() {
        String os = System.getProperty("os.name").toLowerCase();
        boolean isLinux = os.contains("nux") || os.contains("nix");
        assertEquals(isLinux, Utils.isLinux());
    }

    @Test
    public void testEscapeQuotes() {
        assertEquals("test\\\"value\\\"", Utils.escapeQuotes("test\"value\""));
        assertEquals("no quotes", Utils.escapeQuotes("no quotes"));
        assertEquals("", Utils.escapeQuotes(""));
    }

    @Test
    public void testEscapeBackslashes() {
        assertEquals("path\\\\to\\\\file", Utils.escapeBackslashes("path\\to\\file"));
        assertEquals("no backslash", Utils.escapeBackslashes("no backslash"));
    }

    @Test
    public void testGetFileNameWithoutExt() {
        assertEquals("video", Utils.getFileNameWithoutExt("video.mkv"));
        assertEquals("my.video", Utils.getFileNameWithoutExt("my.video.mkv"));
        assertEquals("noext", Utils.getFileNameWithoutExt("noext"));
    }

    @Test
    public void testGetPathWithoutExt() {
        String sep = File.separator;
        String path = "folder" + sep + "video.mkv";
        String expected = "folder" + sep + "video";
        assertEquals(expected, Utils.getPathWithoutExt(path));
    }

    @Test
    public void testPadNumber() {
        assertEquals("001", Utils.padNumber(3, 1));
        assertEquals("01", Utils.padNumber(2, 1));
        assertEquals("123", Utils.padNumber(3, 123));
        assertEquals("1", Utils.padNumber(1, 1));
    }
}
