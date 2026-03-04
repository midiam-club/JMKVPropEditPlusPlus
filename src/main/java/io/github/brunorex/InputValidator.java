package io.github.brunorex;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates input data before processing.
 * Provides methods to validate files, executables, and parameters.
 */
public class InputValidator {

    private static final String[] VALID_MKV_EXTENSIONS = { ".mkv", ".mka", ".mks", ".mk3d", ".webm" };

    /**
     * Validates that a file exists and is readable.
     * 
     * @param file The file to validate
     * @throws MkvPropeditException if the file doesn't exist or isn't readable
     */
    public static void validateFileExists(File file) throws MkvPropeditException {
        if (file == null) {
            throw new MkvPropeditException(ErrorCode.FILE_NOT_FOUND, "null");
        }
        if (!file.exists()) {
            throw new MkvPropeditException(ErrorCode.FILE_NOT_FOUND, file.getAbsolutePath());
        }
        if (!file.canRead()) {
            throw new MkvPropeditException(ErrorCode.FILE_NOT_READABLE, file.getAbsolutePath());
        }
    }

    /**
     * Validates that a file is a valid Matroska file based on extension.
     * 
     * @param file The file to validate
     * @throws MkvPropeditException if the file doesn't have a valid MKV extension
     */
    public static void validateMkvFile(File file) throws MkvPropeditException {
        validateFileExists(file);
        String name = file.getName().toLowerCase();
        boolean valid = false;
        for (String ext : VALID_MKV_EXTENSIONS) {
            if (name.endsWith(ext)) {
                valid = true;
                break;
            }
        }
        if (!valid) {
            throw new MkvPropeditException(ErrorCode.INVALID_FILE_FORMAT, file.getName());
        }
    }

    /**
     * Validates that a list of files is not empty.
     * 
     * @param files The list of files to validate
     * @throws MkvPropeditException if the list is empty or null
     */
    public static void validateFileListNotEmpty(List<?> files) throws MkvPropeditException {
        if (files == null || files.isEmpty()) {
            throw new MkvPropeditException(ErrorCode.EMPTY_FILE_LIST);
        }
    }

    /**
     * Validates that the mkvpropedit executable exists and is executable.
     * 
     * @param executablePath The path to the executable
     * @throws MkvPropeditException if the executable doesn't exist or isn't
     *                              executable
     */
    public static void validateMkvpropeditExecutable(String executablePath) throws MkvPropeditException {
        if (executablePath == null || executablePath.trim().isEmpty()) {
            throw new MkvPropeditException(ErrorCode.MKVPROPEDIT_NOT_FOUND);
        }

        File executable = new File(executablePath);

        // On Windows, also check with .exe extension
        if (!executable.exists() && Utils.isWindows() && !executablePath.toLowerCase().endsWith(".exe")) {
            executable = new File(executablePath + ".exe");
        }

        if (!executable.exists()) {
            // Check if it's in PATH
            String pathEnv = System.getenv("PATH");
            if (pathEnv != null) {
                String[] paths = pathEnv.split(File.pathSeparator);
                boolean found = false;
                for (String path : paths) {
                    File check = new File(path, Utils.isWindows() ? "mkvpropedit.exe" : "mkvpropedit");
                    if (check.exists() && check.canExecute()) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new MkvPropeditException(ErrorCode.MKVPROPEDIT_NOT_FOUND, executablePath);
                }
            } else {
                throw new MkvPropeditException(ErrorCode.MKVPROPEDIT_NOT_FOUND, executablePath);
            }
        }
    }

    /**
     * Validates that a string parameter is not null or empty.
     * 
     * @param value     The value to validate
     * @param paramName The parameter name for error messages
     * @throws MkvPropeditException if the value is null or empty
     */
    public static void validateNotEmpty(String value, String paramName) throws MkvPropeditException {
        if (value == null || value.trim().isEmpty()) {
            throw new MkvPropeditException(ErrorCode.INVALID_PARAMETER, paramName);
        }
    }

    /**
     * Validates multiple files at once.
     * 
     * @param files The files to validate
     * @return List of validation errors (empty if all valid)
     */
    public static List<String> validateFiles(List<File> files) {
        List<String> errors = new ArrayList<>();
        if (files == null || files.isEmpty()) {
            errors.add(LanguageManager.getString("error.input.empty.list"));
            return errors;
        }

        for (File file : files) {
            try {
                validateMkvFile(file);
            } catch (MkvPropeditException e) {
                errors.add(e.getMessage());
            }
        }
        return errors;
    }
}
