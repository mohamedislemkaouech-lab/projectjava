package tn.agricultureai.domain.exception;

import tn.agricultureai.domain.model.MarketReport;

/**
 * Exception thrown when LLM report generation fails.
 * Demonstrates: Specialized exception with retry logic.
 */
public class ReportGenerationException extends AgricultureException {

    private final MarketReport.ReportType reportType;
    private final String llmModelName;
    private final int attemptNumber;

    /**
     * Full constructor
     */
    public ReportGenerationException(
            String message,
            String errorCode,
            Throwable cause,
            MarketReport.ReportType reportType,
            String llmModelName,
            int attemptNumber
    ) {
        super(
                message,
                errorCode,
                cause,
                buildContext(reportType, llmModelName, attemptNumber)
        );
        this.reportType = reportType;
        this.llmModelName = llmModelName;
        this.attemptNumber = attemptNumber;
    }

    /**
     * Simplified constructor
     */
    public ReportGenerationException(
            String message,
            MarketReport.ReportType reportType,
            String llmModelName
    ) {
        this(message, "RPT-001", null, reportType, llmModelName, 1);
    }

    /**
     * Constructor for API timeout
     */
    public static ReportGenerationException apiTimeout(
            String llmModelName,
            int attemptNumber
    ) {
        return new ReportGenerationException(
                "LLM API request timed out",
                "RPT-002",
                null,
                null,
                llmModelName,
                attemptNumber
        );
    }

    /**
     * Constructor for API rate limit
     */
    public static ReportGenerationException rateLimitExceeded(String llmModelName) {
        return new ReportGenerationException(
                "LLM API rate limit exceeded",
                "RPT-003",
                null,
                null,
                llmModelName,
                1
        );
    }

    /**
     * Constructor for invalid API key
     */
    public static ReportGenerationException invalidApiKey(String llmModelName) {
        return new ReportGenerationException(
                "Invalid or missing API key for LLM service",
                "RPT-004",
                null,
                null,
                llmModelName,
                1
        );
    }

    /**
     * Constructor for malformed response
     */
    public static ReportGenerationException malformedResponse(
            String reason,
            MarketReport.ReportType reportType
    ) {
        return new ReportGenerationException(
                "LLM returned malformed response: " + reason,
                "RPT-005",
                null,
                reportType,
                "Unknown",
                1
        );
    }

    // Helper method
    private static String buildContext(
            MarketReport.ReportType reportType,
            String llmModelName,
            int attemptNumber
    ) {
        return String.format(
                "ReportType=%s, LLM=%s, Attempt=%d",
                reportType != null ? reportType.name() : "N/A",
                llmModelName,
                attemptNumber
        );
    }

    // Getters
    public MarketReport.ReportType getReportType() {
        return reportType;
    }

    public String getLlmModelName() {
        return llmModelName;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    @Override
    public String getCategory() {
        return "REPORT";
    }

    @Override
    public ErrorSeverity getSeverity() {
        // API key issues are critical, others are high
        return hasErrorCode("RPT-004") ? ErrorSeverity.CRITICAL : ErrorSeverity.HIGH;
    }

    @Override
    public boolean isRetryable() {
        // Timeout and rate limit can be retried, but not API key issues
        return hasErrorCode("RPT-002") || hasErrorCode("RPT-003");
    }

    /**
     * Check if should wait before retry
     */
    public boolean shouldWaitBeforeRetry() {
        return hasErrorCode("RPT-003"); // Rate limit
    }

    /**
     * Get suggested wait time in seconds
     */
    public int getSuggestedWaitSeconds() {
        if (hasErrorCode("RPT-003")) {
            return 60; // Wait 1 minute for rate limit
        }
        return 5; // Default 5 seconds
    }

    @Override
    public String getUserMessage() {
        String code = getErrorCode();
        return switch (code) {
            case "RPT-002" -> "Report generation timed out. Please try again.";
            case "RPT-003" -> "Too many requests. Please wait a moment and try again.";
            case "RPT-004" -> "Service configuration error. Please contact support.";
            case "RPT-005" -> "Report generation failed due to an unexpected error.";
            default -> "Unable to generate report. Please try again later.";
        };
    }
}