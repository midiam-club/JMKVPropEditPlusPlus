package io.github.brunorex;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.SwingWorker;

/**
 * SwingWorker for processing files in the background.
 * Allows for asynchronous file processing with progress updates and
 * cancellation support.
 */
public class FileProcessorWorker extends SwingWorker<Void, FileProcessorWorker.ProgressInfo> {

    /**
     * Progress information holder.
     */
    public static class ProgressInfo {
        public final int current;
        public final int total;
        public final String currentFile;
        public final String message;

        public ProgressInfo(int current, int total, String currentFile, String message) {
            this.current = current;
            this.total = total;
            this.currentFile = currentFile;
            this.message = message;
        }

        public int getPercentage() {
            if (total == 0)
                return 0;
            return (int) ((current * 100.0) / total);
        }
    }

    /**
     * Interface for file processing logic.
     */
    public interface FileProcessor {
        /**
         * Process a single file.
         * 
         * @param file The file to process
         * @return The output from processing
         * @throws MkvPropeditException if processing fails
         */
        String process(File file) throws MkvPropeditException;
    }

    private final List<File> files;
    private final FileProcessor processor;
    private final Consumer<ProgressInfo> progressCallback;
    private final Consumer<String> outputCallback;
    private final Consumer<Exception> errorCallback;
    private final Runnable completionCallback;

    private int successCount = 0;
    private int errorCount = 0;

    /**
     * Creates a new file processor worker.
     * 
     * @param files              The files to process
     * @param processor          The processing logic
     * @param progressCallback   Callback for progress updates (can be null)
     * @param outputCallback     Callback for output messages (can be null)
     * @param errorCallback      Callback for errors (can be null)
     * @param completionCallback Callback when processing is complete (can be null)
     */
    public FileProcessorWorker(
            List<File> files,
            FileProcessor processor,
            Consumer<ProgressInfo> progressCallback,
            Consumer<String> outputCallback,
            Consumer<Exception> errorCallback,
            Runnable completionCallback) {
        this.files = files;
        this.processor = processor;
        this.progressCallback = progressCallback;
        this.outputCallback = outputCallback;
        this.errorCallback = errorCallback;
        this.completionCallback = completionCallback;
    }

    @Override
    protected Void doInBackground() throws Exception {
        int total = files.size();

        for (int i = 0; i < total && !isCancelled(); i++) {
            File file = files.get(i);

            // Publish progress
            publish(new ProgressInfo(i + 1, total, file.getName(),
                    String.format("Processing %d of %d: %s", i + 1, total, file.getName())));

            try {
                String output = processor.process(file);
                successCount++;

                if (outputCallback != null && output != null && !output.isEmpty()) {
                    outputCallback.accept(output);
                }
            } catch (MkvPropeditException e) {
                errorCount++;
                if (errorCallback != null) {
                    errorCallback.accept(e);
                }
            } catch (Exception e) {
                errorCount++;
                if (errorCallback != null) {
                    errorCallback.accept(new MkvPropeditException(ErrorCode.UNKNOWN_ERROR, e));
                }
            }

            // Update overall progress
            setProgress((int) (((i + 1) * 100.0) / total));
        }

        return null;
    }

    @Override
    protected void process(List<ProgressInfo> chunks) {
        if (progressCallback != null) {
            // Only process the latest progress info
            ProgressInfo latest = chunks.get(chunks.size() - 1);
            progressCallback.accept(latest);
        }
    }

    @Override
    protected void done() {
        if (completionCallback != null) {
            completionCallback.run();
        }
    }

    /**
     * Gets the count of successfully processed files.
     * 
     * @return The success count
     */
    public int getSuccessCount() {
        return successCount;
    }

    /**
     * Gets the count of files that had errors.
     * 
     * @return The error count
     */
    public int getErrorCount() {
        return errorCount;
    }
}
