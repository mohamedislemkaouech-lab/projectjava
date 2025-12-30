package tn.agricultureai.domain.annotation;

import java.lang.annotation.*;

/**
 * Annotation to mark methods or fields that require validation.
 * Demonstrates: Multi-target annotation, validation rules.
 *
 * Usage Examples:
 *
 * // On method parameter
 * public void createExport(@Validated(required = true) ExportData data) { ... }
 *
 * // On field
 * @Validated(min = 0, max = 100)
 * private double confidence;
 *
 * // On method (validates return value)
 * @Validated(notNull = true)
 * public PredictionResult predict() { ... }
 *
 * @author Your Name
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Documented
public @interface Validated {

    /**
     * Whether the value is required (cannot be null)
     */
    boolean required() default false;

    /**
     * Whether null values are allowed
     */
    boolean notNull() default false;

    /**
     * Whether empty strings/collections are allowed
     */
    boolean notEmpty() default false;

    /**
     * Minimum value for numeric types
     */
    double min() default Double.MIN_VALUE;

    /**
     * Maximum value for numeric types
     */
    double max() default Double.MAX_VALUE;

    /**
     * Regex pattern for string validation
     */
    String pattern() default "";

    /**
     * Minimum length for strings/collections
     */
    int minLength() default 0;

    /**
     * Maximum length for strings/collections
     */
    int maxLength() default Integer.MAX_VALUE;

    /**
     * Custom validation message
     */
    String message() default "Validation failed";

    /**
     * Validation groups (for conditional validation)
     */
    String[] groups() default {};

    /**
     * Validation type/category
     */
    ValidationType type() default ValidationType.STANDARD;

    /**
     * Enum for validation types
     */
    enum ValidationType {
        STANDARD("Standard Validation"),
        STRICT("Strict Validation"),
        LENIENT("Lenient Validation"),
        CUSTOM("Custom Validation");

        private final String description;

        ValidationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}