package io.github.brunorex;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link BatchExecutorService}.
 */
class BatchExecutorServiceTest {

    @Test
    void batchResult_recordStoresValues() {
        var result = new BatchExecutorService.BatchResult(
                "file.mkv", "cmd", "output", 0);
        assertEquals("file.mkv", result.filePath());
        assertEquals("cmd", result.commandLine());
        assertEquals("output", result.output());
        assertEquals(0, result.exitCode());
    }

    @Test
    void batchResult_equalityAndHashCode() {
        var a = new BatchExecutorService.BatchResult("f.mkv", "c", "o", 0);
        var b = new BatchExecutorService.BatchResult("f.mkv", "c", "o", 0);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void executeAll_withEmptyLists_doesNotFail() {
        var service = new BatchExecutorService(
                "mkvpropedit",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList());

        List<BatchExecutorService.BatchResult> results = new CopyOnWriteArrayList<>();
        service.executeAll(results::add);
        assertTrue(results.isEmpty());
    }

    @Test
    void executeAll_withNullListener_doesNotFail() {
        var service = new BatchExecutorService(
                "mkvpropedit",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList());
        assertDoesNotThrow(() -> service.executeAll(null));
    }

    @Test
    void isExecutableAvailable_withValidExecutable() {
        // Use "java" which is guaranteed to be on PATH for these tests
        boolean available = BatchExecutorService.isExecutableAvailable("java");
        assertTrue(available);
    }

    @Test
    void isExecutableAvailable_withNonExistentExecutable() {
        boolean available = BatchExecutorService.isExecutableAvailable(
                "this_executable_does_not_exist_12345");
        assertFalse(available);
    }

    @Test
    void executeSingle_returnsErrorForMissingExecutable() {
        var service = new BatchExecutorService(
                "nonexistent_exe_12345",
                Collections.singletonList("--test"),
                Collections.singletonList("--test"),
                Collections.singletonList("file.mkv"));

        BatchExecutorService.BatchResult result = service.executeSingle(0);
        assertEquals("file.mkv", result.filePath());
        assertTrue(result.exitCode() != 0 || result.output().contains("Error"));
    }
}
