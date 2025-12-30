package tn.agricultureai.domain.exception;

import java.time.LocalDateTime;

/**
 * Abstract base exception for all agriculture-related errors.
 * Demonstrates: Abstract class, inheritance hierarchy, common behavior.
 *
 * All custom exceptions extend this base to provide:
 * - Timestamp of when error occurred
 * - Error code for classification
 * - Contextual information
 * - Consistent logging format
 *
 * @author Your Name
 */
public abstract class AgricultureException extends RuntimeException {

    private final String errorCode;
    private final LocalDateTime timestamp;
    private final transient Object context; // Additional context data

    /**
     * Protected constructor - only subclasses can call.
     * Demonstrates: Encapsulation, controlled inheritance.
     *
     * @param message Human-readable error message
     * @param errorCode Unique error code (e.g., "PRED-001")
     * @param cause Original exception that caused this error
     * @param context Additional context information
     */
    protected AgricultureException(
            String message,
            String errorCode,
            Throwable cause,
            Object context
    ) {
        super(message, cause);
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
        this.context = context;
    }

    /**
     * Simplified constructor without cause
     */
    protected AgricultureException(String message, String errorCode, Object context) {
        this(message, errorCode, null, context);
    }

    /**
     * Simplified constructor without context
     */
    protected AgricultureException(String message, String errorCode) {
        this(message, errorCode, null, null);
    }

    // Getters - protected so child classes can access
    public String getErrorCode() {
        return errorCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Object getContext() {
        return context;
    }

    // Protected getter for child classes to check errorCode
    protected boolean hasErrorCode(String code) {
        return this.errorCode != null && this.errorCode.equals(code);
    }

    /**
     * Abstract method - each subclass must define its category.
     * Demonstrates: Abstract method, polymorphism.
     *
     * @return Error category (e.g., "PREDICTION", "DATA", "REPORT")
     */
    public abstract String getCategory();

    /**
     * Abstract method - severity level for logging/monitoring.
     *
     * @return Severity level
     */
    public abstract ErrorSeverity getSeverity();

    /**
     * Get detailed error information formatted for logging.
     * Template method pattern - uses abstract methods.
     */
    public String getDetailedMessage() {
        return String.format(
                "[%s] %s-%s at %s: %s%s",
                getSeverity(),
                getCategory(),
                errorCode,
                timestamp,
                getMessage(),
                context != null ? " | Context: " + context : ""
        );
    }

    /**
     * Check if this exception should be retried
     * Default: false, can be overridden by subclasses
     */
    public boolean isRetryable() {
        return false;
    }

    /**
     * Get user-friendly message (hides technical details)
     */
    public String getUserMessage() {
        return "An error occurred while processing agricultural data. Please try again.";
    }

    @Override
    public String toString() {
        return getDetailedMessage();
    }

    /**
     * Enum for error severity levels
     */
    public enum ErrorSeverity {
        LOW("Low - Informational"),
        MEDIUM("Medium - Warning"),
        HIGH("High - Error"),
        CRITICAL("Critical - System Failure");

        private final String description;

        ErrorSeverity(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}