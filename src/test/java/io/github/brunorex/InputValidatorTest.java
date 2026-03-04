package io.github.brunorex;

import org.junit.Test;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for InputValidator class.
 */
public class InputValidatorTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testValidateFileExists_validFile() throws Exception {
        File testFile = tempFolder.newFile("test.mkv");
        // Should not throw
        InputValidator.validateFileExists(testFile);
    }

    @Test(expected = MkvPropeditException.class)
    public void testValidateFileExists_nullFile() throws Exception {
        InputValidator.validateFileExists(null);
    }

    @Test(expected = MkvPropeditException.class)
    public void testValidateFileExists_nonexistentFile() throws Exception {
        File nonexistent = new File("nonexistent_file_xyz.mkv");
        InputValidator.validateFileExists(nonexistent);
    }

    @Test
    public void testValidateMkvFile_validMkv() throws Exception {
        File mkvFile = tempFolder.newFile("video.mkv");
        // Should not throw
        InputValidator.validateMkvFile(mkvFile);
    }

    @Test
    public void testValidateMkvFile_validMka() throws Exception {
        File mkaFile = tempFolder.newFile("audio.mka");
        // Should not throw
        InputValidator.validateMkvFile(mkaFile);
    }

    @Test
    public void testValidateMkvFile_validWebm() throws Exception {
        File webmFile = tempFolder.newFile("video.webm");
        // Should not throw
        InputValidator.validateMkvFile(webmFile);
    }

    @Test(expected = MkvPropeditException.class)
    public void testValidateMkvFile_invalidExtension() throws Exception {
        File mp4File = tempFolder.newFile("video.mp4");
        InputValidator.validateMkvFile(mp4File);
    }

    @Test
    public void testValidateFileListNotEmpty_validList() throws Exception {
        List<String> files = Arrays.asList("file1.mkv", "file2.mkv");
        // Should not throw
        InputValidator.validateFileListNotEmpty(files);
    }

    @Test(expected = MkvPropeditException.class)
    public void testValidateFileListNotEmpty_emptyList() throws Exception {
        InputValidator.validateFileListNotEmpty(Collections.emptyList());
    }

    @Test(expected = MkvPropeditException.class)
    public void testValidateFileListNotEmpty_nullList() throws Exception {
        InputValidator.validateFileListNotEmpty(null);
    }

    @Test
    public void testValidateNotEmpty_validString() throws Exception {
        // Should not throw
        InputValidator.validateNotEmpty("valid", "testParam");
    }

    @Test(expected = MkvPropeditException.class)
    public void testValidateNotEmpty_emptyString() throws Exception {
        InputValidator.validateNotEmpty("", "testParam");
    }

    @Test(expected = MkvPropeditException.class)
    public void testValidateNotEmpty_nullString() throws Exception {
        InputValidator.validateNotEmpty(null, "testParam");
    }

    @Test(expected = MkvPropeditException.class)
    public void testValidateNotEmpty_whitespaceOnly() throws Exception {
        InputValidator.validateNotEmpty("   ", "testParam");
    }

    @Test
    public void testValidateFiles_allValid() throws Exception {
        File f1 = tempFolder.newFile("video1.mkv");
        File f2 = tempFolder.newFile("video2.mkv");
        List<String> errors = InputValidator.validateFiles(Arrays.asList(f1, f2));
        assertTrue(errors.isEmpty());
    }

    @Test
    public void testValidateFiles_withInvalidFile() throws Exception {
        File valid = tempFolder.newFile("video.mkv");
        File invalid = tempFolder.newFile("video.mp4");
        List<String> errors = InputValidator.validateFiles(Arrays.asList(valid, invalid));
        assertEquals(1, errors.size());
    }
}
