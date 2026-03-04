package io.github.brunorex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.util.function.Consumer;

import javax.swing.SwingWorker;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

/**
 * SwingWorker that downloads the latest mkvpropedit from MKVToolNix,
 * extracts it from the 7z archive, and saves it to a local folder.
 *
 * <p>
 * Provides progress updates via callbacks during download and extraction.
 * </p>
 *
 * <p>
 * Java 21 migration: Uses {@link HttpClient} instead of HttpURLConnection.
 * </p>
 */
public class MkvToolsDownloader extends SwingWorker<File, Integer> {

    // MKVToolNix download base URL
    private static final String MKVTOOLNIX_RELEASES_URL = "https://mkvtoolnix.download/windows/releases/";
    private static final String LATEST_VERSION = "88.0";
    private static final String DOWNLOAD_FILENAME_PATTERN = "mkvtoolnix-64-bit-%s.7z";

    private static final String MKVPROPEDIT_EXE = "mkvpropedit.exe";
    private static final int BUFFER_SIZE = 8192;

    private final File targetDirectory;
    private final Consumer<String> statusCallback;
    private final Consumer<Integer> progressCallback;
    private final Consumer<String> errorCallback;
    private final Runnable completionCallback;

    private volatile boolean cancelled = false;

    // Java 21: Reusable HttpClient with timeouts
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /**
     * Creates a new MkvToolsDownloader.
     *
     * @param targetDirectory    Directory where mkvpropedit.exe will be saved
     * @param statusCallback     Callback for status messages
     * @param progressCallback   Callback for progress percentage (0-100)
     * @param errorCallback      Callback for error messages
     * @param completionCallback Callback when download completes successfully
     */
    public MkvToolsDownloader(File targetDirectory,
            Consumer<String> statusCallback,
            Consumer<Integer> progressCallback,
            Consumer<String> errorCallback,
            Runnable completionCallback) {
        this.targetDirectory = targetDirectory;
        this.statusCallback = statusCallback != null ? statusCallback : s -> {
        };
        this.progressCallback = progressCallback != null ? progressCallback : i -> {
        };
        this.errorCallback = errorCallback != null ? errorCallback : s -> {
        };
        this.completionCallback = completionCallback != null ? completionCallback : () -> {
        };
    }

    @Override
    protected File doInBackground() throws Exception {
        try {
            // Ensure target directory exists
            if (!targetDirectory.exists()) {
                targetDirectory.mkdirs();
            }

            // Build download URL
            var filename = String.format(DOWNLOAD_FILENAME_PATTERN, LATEST_VERSION);
            var downloadUrl = MKVTOOLNIX_RELEASES_URL + LATEST_VERSION + "/" + filename;

            statusCallback.accept(LanguageManager.getString("options.downloading"));

            // Download the 7z file
            var tempFile = downloadFile(downloadUrl);
            if (cancelled || isCancelled()) {
                cleanupTempFile(tempFile);
                return null;
            }

            statusCallback.accept(LanguageManager.getString("options.extracting"));

            // Extract mkvpropedit.exe
            var extractedExe = extractMkvpropedit(tempFile);

            // Cleanup temp file
            cleanupTempFile(tempFile);

            if (extractedExe != null && extractedExe.exists()) {
                statusCallback.accept(LanguageManager.getString("options.download.complete"));
                progressCallback.accept(100);
                return extractedExe;
            } else {
                throw new IOException("Failed to extract mkvpropedit.exe");
            }

        } catch (Exception e) {
            errorCallback.accept(e.getMessage());
            throw e;
        }
    }

    /**
     * Downloads a file from the given URL using Java 21 HttpClient.
     */
    private File downloadFile(String urlString) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .header("User-Agent", "JMkvpropedit")
                .timeout(Duration.ofSeconds(60))
                .GET()
                .build();

        var response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            throw new IOException("HTTP error: " + response.statusCode());
        }

        long fileSize = response.headers()
                .firstValueAsLong("Content-Length")
                .orElse(-1L);

        var tempPath = Files.createTempFile("mkvtoolnix-", ".7z");
        var tempFile = tempPath.toFile();

        try (InputStream in = response.body();
                var out = new FileOutputStream(tempFile)) {

            var buffer = new byte[BUFFER_SIZE];
            long totalBytesRead = 0;
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                if (cancelled || isCancelled()) {
                    return tempFile;
                }

                out.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;

                if (fileSize > 0) {
                    int progress = (int) ((totalBytesRead * 80) / fileSize);
                    progressCallback.accept(progress);
                }
            }
        }

        return tempFile;
    }

    /**
     * Extracts mkvpropedit.exe from the 7z archive.
     */
    private File extractMkvpropedit(File archiveFile) throws IOException {
        var outputFile = new File(targetDirectory, MKVPROPEDIT_EXE);

        try (var sevenZFile = SevenZFile.builder().setFile(archiveFile).get()) {
            SevenZArchiveEntry entry;

            while ((entry = sevenZFile.getNextEntry()) != null) {
                if (cancelled || isCancelled()) {
                    return null;
                }

                // Look for mkvpropedit.exe in any subdirectory
                if (entry.getName().endsWith(MKVPROPEDIT_EXE) && !entry.isDirectory()) {
                    progressCallback.accept(85);

                    try (var out = new FileOutputStream(outputFile)) {
                        var buffer = new byte[BUFFER_SIZE];
                        int bytesRead;

                        while ((bytesRead = sevenZFile.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }

                    progressCallback.accept(95);
                    return outputFile;
                }
            }
        }

        throw new IOException("mkvpropedit.exe not found in archive");
    }

    private void cleanupTempFile(File tempFile) {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Override
    protected void done() {
        if (!cancelled && !isCancelled()) {
            try {
                var result = get();
                if (result != null) {
                    completionCallback.run();
                }
            } catch (Exception e) {
                errorCallback.accept(e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            }
        }
    }

    /**
     * Cancels the download operation.
     */
    public void cancelDownload() {
        cancelled = true;
        cancel(true);
    }

    /**
     * Returns the expected path where mkvpropedit.exe will be saved.
     */
    public File getExpectedOutputFile() {
        return new File(targetDirectory, MKVPROPEDIT_EXE);
    }

    /**
     * Returns the current MKVToolNix version being downloaded.
     */
    public static String getLatestVersion() {
        return LATEST_VERSION;
    }
}
