package tn.agricultureai.domain.exception;

import java.util.List;
import java.util.ArrayList;

/**
 * Exception thrown when data validation fails.
 * Demonstrates: Exception with multiple validation errors, builder pattern.
 */
public class DataValidationException extends AgricultureException {

    private final List<ValidationError> validationErrors;
    private final String fieldName;

    /**
     * Nested record for individual validation errors
     */
    public record ValidationError(
            String field,
            String message,
            Object rejectedValue
    ) {
        @Override
        public String toString() {
            return String.format("%s: %s (value: %s)", field, message, rejectedValue);
        }
    }

    /**
     * Constructor with single validation error
     */
    public DataValidationException(String fieldName, String message, Object rejectedValue) {
        super(
                String.format("Validation failed for field '%s': %s", fieldName, message),
                "VAL-001",
                List.of(new ValidationError(fieldName, message, rejectedValue))
        );
        this.validationErrors = List.of(new ValidationError(fieldName, message, rejectedValue));
        this.fieldName = fieldName;
    }

    /**
     * Constructor with multiple validation errors
     */
    public DataValidationException(List<ValidationError> validationErrors) {
        super(
                buildMessage(validationErrors),
                "VAL-002",
                validationErrors
        );
        this.validationErrors = new ArrayList<>(validationErrors);
        this.fieldName = "multiple";
    }

    /**
     * Static factory method for null value
     */
    public static DataValidationException nullValue(String fieldName) {
        return new DataValidationException(
                fieldName,
                "Value cannot be null",
                null
        );
    }

    /**
     * Static factory method for out of range
     */
    public static DataValidationException outOfRange(
            String fieldName,
            Object value,
            double min,
            double max
    ) {
        return new DataValidationException(
                fieldName,
                String.format("Value must be between %.2f and %.2f", min, max),
                value
        );
    }

    /**
     * Static factory method for invalid format
     */
    public static DataValidationException invalidFormat(
            String fieldName,
            Object value,
            String expectedFormat
    ) {
        return new DataValidationException(
                fieldName,
                String.format("Invalid format, expected: %s", expectedFormat),
                value
        );
    }

    /**
     * Static factory method for empty collection
     */
    public static DataValidationException emptyCollection(String fieldName) {
        return new DataValidationException(
                fieldName,
                "Collection cannot be empty",
                "[]"
        );
    }

    // Helper method to build message from multiple errors
    private static String buildMessage(List<ValidationError> errors) {
        if (errors.isEmpty()) {
            return "Validation failed";
        }
        if (errors.size() == 1) {
            return "Validation failed: " + errors.get(0).message();
        }
        return String.format("Validation failed with %d errors", errors.size());
    }

    // Getters
    public List<ValidationError> getValidationErrors() {
        return List.copyOf(validationErrors);
    }

    public String getFieldName() {
        return fieldName;
    }

    /**
     * Check if specific field has error
     */
    public boolean hasErrorForField(String field) {
        return validationErrors.stream()
                .anyMatch(e -> e.field().equals(field));
    }

    /**
     * Get all error messages concatenated
     */
    public String getAllErrorMessages() {
        return validationErrors.stream()
                .map(ValidationError::message)
                .reduce((a, b) -> a + "; " + b)
                .orElse("No validation errors");
    }

    @Override
    public String getCategory() {
        return "VALIDATION";
    }

    @Override
    public ErrorSeverity getSeverity() {
        return ErrorSeverity.MEDIUM;
    }

    @Override
    public boolean isRetryable() {
        return false; // Validation errors won't be fixed by retry
    }

    @Override
    public String getUserMessage() {
        if (validationErrors.size() == 1) {
            return "Invalid data: " + validationErrors.get(0).message();
        }
        return String.format(
                "Invalid data: %d validation errors found. Please check your input.",
                validationErrors.size()
        );
    }

    @Override
    public String getDetailedMessage() {
        StringBuilder sb = new StringBuilder(super.getDetailedMessage());
        sb.append("\nValidation Errors:");
        for (ValidationError error : validationErrors) {
            sb.append("\n  - ").append(error);
        }
        return sb.toString();
    }
}