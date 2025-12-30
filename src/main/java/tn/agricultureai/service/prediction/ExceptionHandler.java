package tn.agricultureai.service.prediction;

import tn.agricultureai.domain.exception.AgricultureException;
import tn.agricultureai.domain.exception.ReportGenerationException;

import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Utility class for centralized exception handling.
 * Demonstrates: Utility class, polymorphism, functional interfaces.
 */
public final class ExceptionHandler {

    private static final Logger LOGGER = Logger.getLogger(ExceptionHandler.class.getName());

    // Private constructor - utility class
    private ExceptionHandler() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Handle any AgricultureException with automatic logging.
     * Demonstrates: Polymorphism - handles all subclasses.
     *
     * @param exception The exception to handle
     * @param userErrorHandler Callback for user-facing error message
     */
    public static void handle(
            AgricultureException exception,
            Consumer<String> userErrorHandler
    ) {
        // Log detailed message
        logException(exception);

        // Notify user with friendly message
        if (userErrorHandler != null) {
            userErrorHandler.accept(exception.getUserMessage());
        }

        // Trigger retry logic if applicable
        if (exception.isRetryable()) {
            LOGGER.info("Exception is retryable: " + exception.getErrorCode());
        }
    }

    /**
     * Handle exception without user callback
     */
    public static void handle(AgricultureException exception) {
        handle(exception, userMsg -> System.err.println("ERROR: " + userMsg));
    }

    /**
     * Log exception with appropriate level based on severity
     */
    private static void logException(AgricultureException exception) {
        Level level = switch (exception.getSeverity()) {
            case LOW -> Level.INFO;
            case MEDIUM -> Level.WARNING;
            case HIGH, CRITICAL -> Level.SEVERE;
        };

        LOGGER.log(level, exception.getDetailedMessage(), exception);
    }

    /**
     * Check if exception is critical and should stop execution
     */
    public static boolean isCritical(AgricultureException exception) {
        return exception.getSeverity() == AgricultureException.ErrorSeverity.CRITICAL;
    }

    /**
     * Get retry delay for retryable exceptions
     */
    public static int getRetryDelay(AgricultureException exception) {
        if (!exception.isRetryable()) {
            return 0;
        }

        // Special handling for ReportGenerationException
        if (exception instanceof ReportGenerationException rge) {
            return rge.getSuggestedWaitSeconds();
        }

        // Default retry delay based on severity
        return switch (exception.getSeverity()) {
            case LOW, MEDIUM -> 2;
            case HIGH -> 5;
            case CRITICAL -> 10;
        };
    }

    /**
     * Format exception for display in UI
     */
    public static String formatForDisplay(AgricultureException exception) {
        return String.format(
                "⚠️ %s\n\nError Code: %s\nTime: %s\n\n%s",
                exception.getSeverity().getDescription(),
                exception.getCategory() + "-" + exception.getErrorCode(),
                exception.getTimestamp(),
                exception.getUserMessage()
        );
    }

    /**
     * Extract root cause from exception chain
     */
    public static Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }

    /**
     * Create exception summary for reporting
     */
    public static String createSummary(AgricultureException exception) {
        StringBuilder summary = new StringBuilder();
        summary.append("Exception Summary\n");
        summary.append("=================\n");
        summary.append("Category: ").append(exception.getCategory()).append("\n");
        summary.append("Code: ").append(exception.getErrorCode()).append("\n");
        summary.append("Severity: ").append(exception.getSeverity()).append("\n");
        summary.append("Retryable: ").append(exception.isRetryable()).append("\n");
        summary.append("Time: ").append(exception.getTimestamp()).append("\n");
        summary.append("Message: ").append(exception.getMessage()).append("\n");

        if (exception.getContext() != null) {
            summary.append("Context: ").append(exception.getContext()).append("\n");
        }

        return summary.toString();
    }
}