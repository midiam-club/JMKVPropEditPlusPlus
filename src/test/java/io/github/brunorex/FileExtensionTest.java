package io.github.brunorex;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for FileExtension enum.
 */
public class FileExtensionTest {

    @Test
    public void testGetExtension() {
        assertEquals(".mkv", FileExtension.MKV.getExtension());
        assertEquals(".mka", FileExtension.MKA.getExtension());
        assertEquals(".webm", FileExtension.WEBM.getExtension());
    }

    @Test
    public void testMatches_matchingExtension() {
        assertTrue(FileExtension.MKV.matches("video.mkv"));
        assertTrue(FileExtension.MKV.matches("video.MKV"));
        assertTrue(FileExtension.MKA.matches("audio.mka"));
    }

    @Test
    public void testMatches_nonMatchingExtension() {
        assertFalse(FileExtension.MKV.matches("video.mp4"));
        assertFalse(FileExtension.MKV.matches("video.mka"));
    }

    @Test
    public void testMatches_null() {
        assertFalse(FileExtension.MKV.matches(null));
    }

    @Test
    public void testIsMatroskaFile_validFiles() {
        assertTrue(FileExtension.isMatroskaFile("video.mkv"));
        assertTrue(FileExtension.isMatroskaFile("audio.mka"));
        assertTrue(FileExtension.isMatroskaFile("subs.mks"));
        assertTrue(FileExtension.isMatroskaFile("video.mk3d"));
        assertTrue(FileExtension.isMatroskaFile("video.webm"));
    }

    @Test
    public void testIsMatroskaFile_invalidFiles() {
        assertFalse(FileExtension.isMatroskaFile("video.mp4"));
        assertFalse(FileExtension.isMatroskaFile("video.avi"));
        assertFalse(FileExtension.isMatroskaFile("audio.mp3"));
    }

    @Test
    public void testGetMatroskaExtensions() {
        FileExtension[] exts = FileExtension.getMatroskaExtensions();
        assertEquals(5, exts.length);
    }
}
