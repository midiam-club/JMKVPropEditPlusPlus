package io.github.brunorex;

/**
 * Custom exception for JMKVPropedit++ operations.
 * Provides structured error handling with error codes and localized messages.
 */
public class MkvPropeditException extends Exception {
    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;
    private final String details;

    /**
     * Creates a new exception with the specified error code.
     * 
     * @param errorCode The error code
     */
    public MkvPropeditException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = null;
    }

    /**
     * Creates a new exception with the specified error code and additional details.
     * 
     * @param errorCode The error code
     * @param details   Additional details about the error
     */
    public MkvPropeditException(ErrorCode errorCode, String details) {
        super(errorCode.getMessage() + (details != null ? ": " + details : ""));
        this.errorCode = errorCode;
        this.details = details;
    }

    /**
     * Creates a new exception with the specified error code and cause.
     * 
     * @param errorCode The error code
     * @param cause     The underlying cause
     */
    public MkvPropeditException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.details = cause.getMessage();
    }

    /**
     * Creates a new exception with the specified error code, details, and cause.
     * 
     * @param errorCode The error code
     * @param details   Additional details about the error
     * @param cause     The underlying cause
     */
    public MkvPropeditException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode.getMessage() + (details != null ? ": " + details : ""), cause);
        this.errorCode = errorCode;
        this.details = details;
    }

    /**
     * Gets the error code.
     * 
     * @return The error code
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Gets additional details about the error.
     * 
     * @return Additional details, or null if none
     */
    public String getDetails() {
        return details;
    }

    /**
     * Gets the localized error message without details.
     * 
     * @return The base localized message
     */
    public String getLocalizedBaseMessage() {
        return errorCode.getMessage();
    }
}
