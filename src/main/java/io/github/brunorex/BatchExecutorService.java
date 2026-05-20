package io.github.brunorex;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes mkvpropedit commands in parallel and captures their output.
 * <p>
 * This service is UI-agnostic. It receives command data, runs the external
 * process, and returns structured results. The caller is responsible for
 * updating the UI (e.g. via {@link javax.swing.SwingUtilities#invokeLater}).
 * </p>
 */
public class BatchExecutorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchExecutorService.class);

    private final String executablePath;
    private final List<String> cmdLineBatch;
    private final List<String> cmdLineBatchOpt;
    private final List<String> targetFiles;

    public BatchExecutorService(String executablePath,
            List<String> cmdLineBatch,
            List<String> cmdLineBatchOpt,
            List<String> targetFiles) {
        this.executablePath = executablePath;
        this.cmdLineBatch = cmdLineBatch;
        this.cmdLineBatchOpt = cmdLineBatchOpt;
        this.targetFiles = targetFiles;
    }

    /**
     * Executes all commands in parallel using a fixed thread pool.
     *
     * @param listener callback that receives each file's result as it completes
     */
    public void executeAll(BatchResultListener listener) {
        int cores = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
        ExecutorService executor = Executors.newFixedThreadPool(cores);

        for (int i = 0; i < cmdLineBatch.size(); i++) {
            final int index = i;
            executor.submit(() -> {
                BatchResult result = executeSingle(index);
                if (listener != null) {
                    listener.onResult(result);
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Executes a single mkvpropedit command for the given file index.
     *
     * @param index the file index
     * @return the result containing output, command line, and file reference
     */
    public BatchResult executeSingle(int index) {
        Path optFile = null;
        try {
            optFile = Files.createTempFile("jmkvpropedit_opts_", ".json");
            optFile.toFile().deleteOnExit();

            String[] optFileContents = Commandline.translateCommandline(cmdLineBatchOpt.get(index));
            String jsonContent = buildJsonOptions(optFileContents);
            Files.writeString(optFile, jsonContent, StandardCharsets.UTF_8);

            ProcessBuilder pb = new ProcessBuilder(executablePath,
                    "@" + optFile.toAbsolutePath().toString());
            pb.redirectErrorStream(true);

            Process process = pb.start();
            String processOutput = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            int exitCode = process.waitFor();

            return new BatchResult(
                    targetFiles.get(index),
                    cmdLineBatch.get(index),
                    processOutput,
                    exitCode);

        } catch (IOException e) {
            LOGGER.warn("Error executing mkvpropedit for file: {}", targetFiles.get(index), e);
            return new BatchResult(targetFiles.get(index), cmdLineBatch.get(index),
                    "Error: " + e.getMessage(), -1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new BatchResult(targetFiles.get(index), cmdLineBatch.get(index),
                    "Interrupted", -1);
        } finally {
            if (optFile != null) {
                try {
                    Files.deleteIfExists(optFile);
                } catch (IOException ignored) {
                }
            }
        }
    }

    private String buildJsonOptions(String[] args) {
        var sb = new StringBuilder("[\n");
        int max = args.length - 1;
        for (int i = 0; i < args.length; i++) {
            var content = Utils.fixEscapedQuotes(args[i]);
            sb.append("  \"").append(content).append("\"");
            if (i != max) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("]\n");
        return sb.toString();
    }

    /**
     * Checks whether the given executable can be launched successfully.
     * This method blocks the calling thread; do not call it on the EDT.
     *
     * @param executablePath the executable to test
     * @return true if the process starts and exits without IOException
     */
    public static boolean isExecutableAvailable(String executablePath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(executablePath);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Drain the output stream to avoid deadlock on Windows
            new Thread(() -> {
                try {
                    process.getInputStream().transferTo(java.io.OutputStream.nullOutputStream());
                } catch (IOException ignored) {
                }
            }).start();

            return process.waitFor(5, TimeUnit.SECONDS);
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    /**
     * Result of executing mkvpropedit for a single file.
     */
    public record BatchResult(String filePath, String commandLine, String output, int exitCode) {
    }

    /**
     * Listener called when a single file finishes processing.
     */
    @FunctionalInterface
    public interface BatchResultListener {
        void onResult(BatchResult result);
    }
}
