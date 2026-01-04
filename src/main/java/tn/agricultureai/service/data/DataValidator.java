package tn.agricultureai.service.data;

import lombok.extern.slf4j.Slf4j;
import tn.agricultureai.domain.exception.DataValidationException;
import tn.agricultureai.domain.model.*;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;

/**
 * Validator for export data and predictions.
 * Demonstrates: Validation logic, functional interfaces, business rules.
 *
 * @author Your Name
 */
@Slf4j
public class DataValidator {

    // Validation constants
    private static final double MIN_PRICE = 0.01;
    private static final double MAX_PRICE = 1000.0;
    private static final double MIN_QUANTITY = 0.01;
    private static final double MAX_QUANTITY = 10000.0;
    private static final int MAX_DATE_FUTURE_DAYS = 0;
    private static final int MAX_DATE_PAST_YEARS = 10;
    private static final double PRICE_DEVIATION_THRESHOLD = 0.5; // 50%

    /**
     * Validate export data
     * @throws DataValidationException if validation fails
     */
    public void validate(ExportData data) {
        List<DataValidationException.ValidationError> errors = new ArrayList<>();

        // Validate product type
        if (data.productType() == null) {
            errors.add(new DataValidationException.ValidationError(
                    "productType",
                    "Product type is required",
                    null
            ));
        }

        // Validate destination
        if (data.destination() == null) {
            errors.add(new DataValidationException.ValidationError(
                    "destination",
                    "Destination country is required",
                    null
            ));
        }

        // Validate price
        validatePrice(data.pricePerUnit(), errors);

        // Validate quantity
        validateQuantity(data.quantity(), errors);

        // Validate date
        validateExportDate(data.exportDate(), errors);

        // Validate source
        if (data.source() == null || data.source().isBlank()) {
            errors.add(new DataValidationException.ValidationError(
                    "source",
                    "Data source must be specified",
                    data.source()
            ));
        }

        // Business rule: Check price reasonability for product type
        if (data.productType() != null && errors.isEmpty()) {
            validatePriceReasonability(data, errors);
        }

        // Throw if errors found
        if (!errors.isEmpty()) {
            log.error("Validation failed with {} errors", errors.size());
            throw new DataValidationException(errors);
        }

        log.debug("Validation successful for export data");
    }

    /**
     * Validate price field
     */
    private void validatePrice(double pricePerUnit, List<DataValidationException.ValidationError> errors) {
        if (pricePerUnit < MIN_PRICE) {
            errors.add(new DataValidationException.ValidationError(
                    "pricePerUnit",
                    String.format("Price must be at least %.2f EUR/kg", MIN_PRICE),
                    pricePerUnit
            ));
        }

        if (pricePerUnit > MAX_PRICE) {
            errors.add(new DataValidationException.ValidationError(
                    "pricePerUnit",
                    String.format("Price cannot exceed %.2f EUR/kg", MAX_PRICE),
                    pricePerUnit
            ));
        }

        if (Double.isNaN(pricePerUnit) || Double.isInfinite(pricePerUnit)) {
            errors.add(new DataValidationException.ValidationError(
                    "pricePerUnit",
                    "Price must be a valid number",
                    pricePerUnit
            ));
        }
    }

    /**
     * Validate quantity field
     */
    private void validateQuantity(double quantity, List<DataValidationException.ValidationError> errors) {
        if (quantity < MIN_QUANTITY) {
            errors.add(new DataValidationException.ValidationError(
                    "quantity",
                    String.format("Quantity must be at least %.2f tons", MIN_QUANTITY),
                    quantity
            ));
        }

        if (quantity > MAX_QUANTITY) {
            errors.add(new DataValidationException.ValidationError(
                    "quantity",
                    String.format("Quantity cannot exceed %.2f tons", MAX_QUANTITY),
                    quantity
            ));
        }

        if (Double.isNaN(quantity) || Double.isInfinite(quantity)) {
            errors.add(new DataValidationException.ValidationError(
                    "quantity",
                    "Quantity must be a valid number",
                    quantity
            ));
        }
    }

    /**
     * Validate export date
     */
    private void validateExportDate(LocalDate exportDate, List<DataValidationException.ValidationError> errors) {
        if (exportDate == null) {
            errors.add(new DataValidationException.ValidationError(
                    "exportDate",
                    "Export date is required",
                    null
            ));
            return;
        }

        LocalDate now = LocalDate.now();
        LocalDate maxFuture = now.plusDays(MAX_DATE_FUTURE_DAYS);
        LocalDate maxPast = now.minusYears(MAX_DATE_PAST_YEARS);

        if (exportDate.isAfter(maxFuture)) {
            errors.add(new DataValidationException.ValidationError(
                    "exportDate",
                    "Export date cannot be in the future",
                    exportDate
            ));
        }

        if (exportDate.isBefore(maxPast)) {
            errors.add(new DataValidationException.ValidationError(
                    "exportDate",
                    String.format("Export date cannot be older than %d years", MAX_DATE_PAST_YEARS),
                    exportDate
            ));
        }
    }

    /**
     * Validate price reasonability for product type
     */
    private void validatePriceReasonability(
            ExportData data,
            List<DataValidationException.ValidationError> errors
    ) {
        double avgPrice = data.productType().getAveragePrice();
        double deviation = Math.abs(data.pricePerUnit() - avgPrice) / avgPrice;

        if (deviation > PRICE_DEVIATION_THRESHOLD) {
            log.warn("Price {} for {} deviates significantly ({:.1f}%) from average {}",
                    data.pricePerUnit(),
                    data.productType(),
                    deviation * 100,
                    avgPrice);

            // Add warning but don't fail validation
            // You can uncomment to make it a hard error:
            /*
            errors.add(new DataValidationException.ValidationError(
                    "pricePerUnit",
                    String.format("Price deviates too much from average (%.2f EUR/kg)", avgPrice),
                    data.pricePerUnit()
            ));
            */
        }
    }

    /**
     * Validate multiple records
     */
    public void validateBatch(List<ExportData> dataList) {
        List<DataValidationException.ValidationError> allErrors = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i++) {
            try {
                validate(dataList.get(i));
            } catch (DataValidationException e) {
                // Add errors with record index
                for (var error : e.getValidationErrors()) {
                    allErrors.add(new DataValidationException.ValidationError(
                            "record[" + i + "]." + error.field(),
                            error.message(),
                            error.rejectedValue()
                    ));
                }
            }
        }

        if (!allErrors.isEmpty()) {
            throw new DataValidationException(allErrors);
        }
    }

    /**
     * Check if data passes a custom validation rule
     */
    public boolean validateCustomRule(ExportData data, Predicate<ExportData> rule) {
        try {
            return rule.test(data);
        } catch (Exception e) {
            log.error("Custom validation rule failed", e);
            return false;
        }
    }

    /**
     * Validate prediction result
     */
    public void validatePrediction(PredictionResult prediction) {
        List<DataValidationException.ValidationError> errors = new ArrayList<>();

        // Validate product type
        if (prediction.productType() == null) {
            errors.add(new DataValidationException.ValidationError(
                    "productType",
                    "Product type is required",
                    null
            ));
        }

        // Validate destination
        if (prediction.destination() == null) {
            errors.add(new DataValidationException.ValidationError(
                    "destination",
                    "Destination is required",
                    null
            ));
        }

        // Validate predicted price
        if (prediction.predictedPrice() <= 0) {
            errors.add(new DataValidationException.ValidationError(
                    "predictedPrice",
                    "Predicted price must be positive",
                    prediction.predictedPrice()
            ));
        }

        if (prediction.predictedPrice() > MAX_PRICE) {
            errors.add(new DataValidationException.ValidationError(
                    "predictedPrice",
                    String.format("Predicted price cannot exceed %.2f EUR/kg", MAX_PRICE),
                    prediction.predictedPrice()
            ));
        }

        // Validate confidence score
        if (prediction.confidenceScore() < 0.0 || prediction.confidenceScore() > 1.0) {
            errors.add(new DataValidationException.ValidationError(
                    "confidenceScore",
                    "Confidence score must be between 0.0 and 1.0",
                    prediction.confidenceScore()
            ));
        }

        // Validate model name
        if (prediction.modelName() == null || prediction.modelName().isBlank()) {
            errors.add(new DataValidationException.ValidationError(
                    "modelName",
                    "Model name is required",
                    prediction.modelName()
            ));
        }

        // Throw if errors found
        if (!errors.isEmpty()) {
            throw new DataValidationException(errors);
        }
    }

    /**
     * Validate market report
     */
    public void validateReport(MarketReport report) {
        List<DataValidationException.ValidationError> errors = new ArrayList<>();

        // Validate title
        if (report.title() == null || report.title().isBlank()) {
            errors.add(new DataValidationException.ValidationError(
                    "title",
                    "Report title is required",
                    report.title()
            ));
        }

        // Validate content
        if (report.content() == null || report.content().isBlank()) {
            errors.add(new DataValidationException.ValidationError(
                    "content",
                    "Report content is required",
                    report.content()
            ));
        }

        // Validate content length (minimum)
        if (report.content() != null && report.content().length() < 50) {
            errors.add(new DataValidationException.ValidationError(
                    "content",
                    "Report content must be at least 50 characters",
                    report.content().length()
            ));
        }

        // Validate report type
        if (report.reportType() == null) {
            errors.add(new DataValidationException.ValidationError(
                    "reportType",
                    "Report type is required",
                    null
            ));
        }

        // Validate predictions list
        if (report.predictions() == null) {
            errors.add(new DataValidationException.ValidationError(
                    "predictions",
                    "Predictions list cannot be null",
                    null
            ));
        }

        // Throw if errors found
        if (!errors.isEmpty()) {
            throw new DataValidationException(errors);
        }
    }

    /**
     * Quick validation - returns boolean instead of throwing
     */
    public boolean isValid(ExportData data) {
        try {
            validate(data);
            return true;
        } catch (DataValidationException e) {
            return false;
        }
    }

    /**
     * Quick validation for prediction
     */
    public boolean isValidPrediction(PredictionResult prediction) {
        try {
            validatePrediction(prediction);
            return true;
        } catch (DataValidationException e) {
            return false;
        }
    }

    /**
     * Get validation errors without throwing
     */
    public List<String> getValidationErrors(ExportData data) {
        try {
            validate(data);
            return Collections.emptyList();
        } catch (DataValidationException e) {
            return e.getValidationErrors().stream()
                    .map(error -> error.field() + ": " + error.message())
                    .toList();
        }
    }

    /**
     * Validate with custom business rules
     */
    public void validateWithRules(
            ExportData data,
            List<ValidationRule> customRules
    ) {
        // First, run standard validation
        validate(data);

        // Then apply custom rules
        List<DataValidationException.ValidationError> errors = new ArrayList<>();

        for (ValidationRule rule : customRules) {
            if (!rule.test(data)) {
                errors.add(new DataValidationException.ValidationError(
                        rule.fieldName(),
                        rule.errorMessage(),
                        rule.extractValue(data)
                ));
            }
        }

        if (!errors.isEmpty()) {
            throw new DataValidationException(errors);
        }
    }

    /**
     * Functional interface for custom validation rules
     */
    @FunctionalInterface
    public interface ValidationRule {
        boolean test(ExportData data);

        default String fieldName() {
            return "custom";
        }

        default String errorMessage() {
            return "Custom validation failed";
        }

        default Object extractValue(ExportData data) {
            return null;
        }
    }

    /**
     * Pre-defined validation rules
     */
    public static class ValidationRules {

        /**
         * Rule: EU exports must be above minimum price
         */
        public static ValidationRule euMinimumPrice(double minPrice) {
            return new ValidationRule() {
                @Override
                public boolean test(ExportData data) {
                    if (!data.isEuExport()) {
                        return true; // Rule doesn't apply
                    }
                    return data.pricePerUnit() >= minPrice;
                }

                @Override
                public String fieldName() {
                    return "pricePerUnit";
                }

                @Override
                public String errorMessage() {
                    return String.format("EU exports must have price >= %.2f EUR/kg", minPrice);
                }

                @Override
                public Object extractValue(ExportData data) {
                    return data.pricePerUnit();
                }
            };
        }

        /**
         * Rule: High-value products must have minimum quantity
         */
        public static ValidationRule highValueMinimumQuantity(double minQuantity) {
            return new ValidationRule() {
                @Override
                public boolean test(ExportData data) {
                    if (!data.productType().isHighValue()) {
                        return true;
                    }
                    return data.quantity() >= minQuantity;
                }

                @Override
                public String fieldName() {
                    return "quantity";
                }

                @Override
                public String errorMessage() {
                    return String.format("High-value products require minimum %.2f tons", minQuantity);
                }

                @Override
                public Object extractValue(ExportData data) {
                    return data.quantity();
                }
            };
        }

        /**
         * Rule: Recent exports only (within last N days)
         */
        public static ValidationRule recentExportsOnly(int maxDaysOld) {
            return new ValidationRule() {
                @Override
                public boolean test(ExportData data) {
                    LocalDate cutoff = LocalDate.now().minusDays(maxDaysOld);
                    return !data.exportDate().isBefore(cutoff);
                }

                @Override
                public String fieldName() {
                    return "exportDate";
                }

                @Override
                public String errorMessage() {
                    return String.format("Export must be within last %d days", maxDaysOld);
                }

                @Override
                public Object extractValue(ExportData data) {
                    return data.exportDate();
                }
            };
        }
    }
}