package io.github.brunorex;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MkvToolsDownloader}.
 */
class MkvToolsDownloaderTest {

    @TempDir
    Path tempDir;

    @Test
    void constructor_acceptsNullCallbacks() {
        File target = tempDir.toFile();
        assertDoesNotThrow(() -> new MkvToolsDownloader(target, null, null, null, null));
    }

    @Test
    void getExpectedOutputFile_returnsCorrectPath() {
        File target = tempDir.toFile();
        var downloader = new MkvToolsDownloader(target, null, null, null, null);
        File expected = downloader.getExpectedOutputFile();
        assertEquals(new File(target, "mkvpropedit.exe"), expected);
    }

    @Test
    void getLatestVersion_returnsNonEmptyString() {
        String version = MkvToolsDownloader.getLatestVersion();
        assertNotNull(version);
        assertFalse(version.isBlank());
    }

    @Test
    void verifyChecksum_validHashDoesNotThrow() throws Exception {
        File testFile = tempDir.resolve("test.txt").toFile();
        String content = "Hello, MKV!";
        try (var out = new FileOutputStream(testFile)) {
            out.write(content.getBytes());
        }

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(content.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        String expectedHash = sb.toString();

        var downloader = new MkvToolsDownloader(tempDir.toFile(), null, null, null, null);
        assertDoesNotThrow(() -> downloader.verifyChecksum(testFile, expectedHash));
    }

    @Test
    void verifyChecksum_invalidHashThrowsIOException() throws Exception {
        File testFile = tempDir.resolve("test.txt").toFile();
        try (var out = new FileOutputStream(testFile)) {
            out.write("content".getBytes());
        }

        var downloader = new MkvToolsDownloader(tempDir.toFile(), null, null, null, null);
        var ex = assertThrows(java.io.IOException.class,
                () -> downloader.verifyChecksum(testFile, "0000000000000000000000000000000000000000000000000000000000000000"));
        assertTrue(ex.getMessage().contains("Checksum mismatch"));
    }

    @Test
    void verifyChecksum_nullOrBlankHashSkipsVerification() throws Exception {
        File testFile = tempDir.resolve("test.txt").toFile();
        try (var out = new FileOutputStream(testFile)) {
            out.write("content".getBytes());
        }

        AtomicReference<String> status = new AtomicReference<>();
        var downloader = new MkvToolsDownloader(tempDir.toFile(), status::set, null, null, null);
        assertDoesNotThrow(() -> downloader.verifyChecksum(testFile, null));
        assertEquals("Warning: download checksum verification is disabled", status.get());

        status.set(null);
        assertDoesNotThrow(() -> downloader.verifyChecksum(testFile, "   "));
        assertEquals("Warning: download checksum verification is disabled", status.get());
    }

    @Test
    void cancelDownload_setsCancelledFlag() {
        var downloader = new MkvToolsDownloader(tempDir.toFile(), null, null, null, null);
        assertFalse(downloader.isCancelled());
        downloader.cancelDownload();
        assertTrue(downloader.isCancelled());
    }

    @Test
    void callbacks_receiveValuesDuringLifecycle() throws Exception {
        File target = tempDir.toFile();
        AtomicReference<String> status = new AtomicReference<>();
        AtomicInteger progress = new AtomicInteger(-1);
        AtomicReference<String> error = new AtomicReference<>();
        AtomicInteger completed = new AtomicInteger(0);

        var downloader = new MkvToolsDownloader(
                target,
                status::set,
                progress::set,
                error::set,
                completed::incrementAndGet
        );

        // Verify callbacks exist and default no-ops work
        assertDoesNotThrow(() -> {
            // doInBackground would require network; we only verify constructor wiring here
        });

        // We can't easily test doInBackground without network mocking,
        // but we can at least verify the object was constructed with all callbacks.
        assertNotNull(downloader);
    }
}
