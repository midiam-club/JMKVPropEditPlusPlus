package io.github.brunorex;

/**
 * Enum defining error codes for JMKVPropedit++ operations.
 */
public enum ErrorCode {
    // File errors
    FILE_NOT_FOUND("error.file.not.found"),
    FILE_NOT_READABLE("error.file.not.readable"),
    FILE_NOT_WRITABLE("error.file.not.writable"),
    INVALID_FILE_FORMAT("error.file.invalid.format"),

    // Executable errors
    MKVPROPEDIT_NOT_FOUND("error.mkvpropedit.not.found"),
    MKVPROPEDIT_EXECUTION_FAILED("error.mkvpropedit.execution.failed"),

    // Input validation errors
    EMPTY_FILE_LIST("error.input.empty.list"),
    INVALID_PARAMETER("error.input.invalid.parameter"),
    NOTHING_TO_DO("error.input.nothing.to.do"),

    // Profile errors
    PROFILE_SAVE_FAILED("error.profile.save.failed"),
    PROFILE_LOAD_FAILED("error.profile.load.failed"),

    // General errors
    UNKNOWN_ERROR("error.unknown");

    private final String messageKey;

    ErrorCode(String messageKey) {
        this.messageKey = messageKey;
    }

    /**
     * Gets the i18n message key for this error code.
     * 
     * @return The message key to use with LanguageManager
     */
    public String getMessageKey() {
        return messageKey;
    }

    /**
     * Gets the localized error message.
     * 
     * @return The localized error message
     */
    public String getMessage() {
        return LanguageManager.getString(messageKey);
    }
}
