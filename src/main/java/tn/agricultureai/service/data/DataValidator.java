package tn.agricultureai.service.data;

import lombok.extern.slf4j.Slf4j;
import tn.agricultureai.domain.exception.DataValidationException;
import tn.agricultureai.domain.model.*;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;

@Slf4j
public class DataValidator {

    private static final double MIN_PRICE = 0.01;
    private static final double MAX_PRICE = 1000.0;
    private static final double MIN_QUANTITY = 0.01;
    private static final double MAX_QUANTITY = 10000.0;
    private static final int MAX_DATE_FUTURE_DAYS = 0;
    private static final int MAX_DATE_PAST_YEARS = 10;
    private static final double PRICE_DEVIATION_THRESHOLD = 0.5;

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

        // Validate destination - use destinationCountry()
        if (data.destinationCountry() == null || data.destinationCountry().isBlank()) {
            errors.add(new DataValidationException.ValidationError(
                    "destinationCountry",
                    "Destination country is required",
                    data.destinationCountry()
            ));
        }

        // Validate price - use pricePerTon()
        validatePrice(data.pricePerTon(), errors);

        // Validate quantity - use volume()
        validateQuantity(data.volume(), errors);

        // Validate date - use date()
        validateExportDate(data.date(), errors);

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

    private void validatePrice(double pricePerTon, List<DataValidationException.ValidationError> errors) {
        if (pricePerTon < MIN_PRICE) {
            errors.add(new DataValidationException.ValidationError(
                    "pricePerTon",
                    String.format("Price must be at least %.2f TND/kg", MIN_PRICE),
                    pricePerTon
            ));
        }

        if (pricePerTon > MAX_PRICE) {
            errors.add(new DataValidationException.ValidationError(
                    "pricePerTon",
                    String.format("Price cannot exceed %.2f TND/kg", MAX_PRICE),
                    pricePerTon
            ));
        }

        if (Double.isNaN(pricePerTon) || Double.isInfinite(pricePerTon)) {
            errors.add(new DataValidationException.ValidationError(
                    "pricePerTon",
                    "Price must be a valid number",
                    pricePerTon
            ));
        }
    }

    private void validateQuantity(double quantity, List<DataValidationException.ValidationError> errors) {
        if (quantity < MIN_QUANTITY) {
            errors.add(new DataValidationException.ValidationError(
                    "volume",
                    String.format("Quantity must be at least %.2f tons", MIN_QUANTITY),
                    quantity
            ));
        }

        if (quantity > MAX_QUANTITY) {
            errors.add(new DataValidationException.ValidationError(
                    "volume",
                    String.format("Quantity cannot exceed %.2f tons", MAX_QUANTITY),
                    quantity
            ));
        }

        if (Double.isNaN(quantity) || Double.isInfinite(quantity)) {
            errors.add(new DataValidationException.ValidationError(
                    "volume",
                    "Quantity must be a valid number",
                    quantity
            ));
        }
    }

    private void validateExportDate(LocalDate exportDate, List<DataValidationException.ValidationError> errors) {
        if (exportDate == null) {
            errors.add(new DataValidationException.ValidationError(
                    "date",
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
                    "date",
                    "Export date cannot be in the future",
                    exportDate
            ));
        }

        if (exportDate.isBefore(maxPast)) {
            errors.add(new DataValidationException.ValidationError(
                    "date",
                    String.format("Export date cannot be older than %d years", MAX_DATE_PAST_YEARS),
                    exportDate
            ));
        }
    }

    private void validatePriceReasonability(
            ExportData data,
            List<DataValidationException.ValidationError> errors
    ) {
        double avgPrice = data.productType().getAveragePrice();
        double deviation = Math.abs(data.pricePerTon() - avgPrice) / avgPrice;

        if (deviation > PRICE_DEVIATION_THRESHOLD) {
            log.warn("Price {} for {} deviates significantly ({:.1f}%) from average {}",
                    data.pricePerTon(),
                    data.productType(),
                    deviation * 100,
                    avgPrice);
        }
    }

    public void validateBatch(List<ExportData> dataList) {
        List<DataValidationException.ValidationError> allErrors = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i++) {
            try {
                validate(dataList.get(i));
            } catch (DataValidationException e) {
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

    public boolean validateCustomRule(ExportData data, Predicate<ExportData> rule) {
        try {
            return rule.test(data);
        } catch (Exception e) {
            log.error("Custom validation rule failed", e);
            return false;
        }
    }

    public void validatePrediction(PredictionResult prediction) {
        List<DataValidationException.ValidationError> errors = new ArrayList<>();

        if (prediction.productType() == null) {
            errors.add(new DataValidationException.ValidationError(
                    "productType",
                    "Product type is required",
                    null
            ));
        }

        if (prediction.destination() == null) {
            errors.add(new DataValidationException.ValidationError(
                    "destination",
                    "Destination is required",
                    null
            ));
        }

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
                    String.format("Predicted price cannot exceed %.2f TND/kg", MAX_PRICE),
                    prediction.predictedPrice()
            ));
        }

        if (prediction.confidenceScore() < 0.0 || prediction.confidenceScore() > 1.0) {
            errors.add(new DataValidationException.ValidationError(
                    "confidenceScore",
                    "Confidence score must be between 0.0 and 1.0",
                    prediction.confidenceScore()
            ));
        }

        if (prediction.modelName() == null || prediction.modelName().isBlank()) {
            errors.add(new DataValidationException.ValidationError(
                    "modelName",
                    "Model name is required",
                    prediction.modelName()
            ));
        }

        if (!errors.isEmpty()) {
            throw new DataValidationException(errors);
        }
    }

    public void validateReport(MarketReport report) {
        List<DataValidationException.ValidationError> errors = new ArrayList<>();

        if (report.title() == null || report.title().isBlank()) {
            errors.add(new DataValidationException.ValidationError(
                    "title",
                    "Report title is required",
                    report.title()
            ));
        }

        if (report.content() == null || report.content().isBlank()) {
            errors.add(new DataValidationException.ValidationError(
                    "content",
                    "Report content is required",
                    report.content()
            ));
        }

        if (report.content() != null && report.content().length() < 50) {
            errors.add(new DataValidationException.ValidationError(
                    "content",
                    "Report content must be at least 50 characters",
                    report.content().length()
            ));
        }

        if (report.reportType() == null) {
            errors.add(new DataValidationException.ValidationError(
                    "reportType",
                    "Report type is required",
                    null
            ));
        }

        if (report.predictions() == null) {
            errors.add(new DataValidationException.ValidationError(
                    "predictions",
                    "Predictions list cannot be null",
                    null
            ));
        }

        if (!errors.isEmpty()) {
            throw new DataValidationException(errors);
        }
    }

    public boolean isValid(ExportData data) {
        try {
            validate(data);
            return true;
        } catch (DataValidationException e) {
            return false;
        }
    }

    public boolean isValidPrediction(PredictionResult prediction) {
        try {
            validatePrediction(prediction);
            return true;
        } catch (DataValidationException e) {
            return false;
        }
    }

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

    public void validateWithRules(
            ExportData data,
            List<ValidationRule> customRules
    ) {
        validate(data);

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

    public static class ValidationRules {
        public static ValidationRule euMinimumPrice(double minPrice) {
            return new ValidationRule() {
                @Override
                public boolean test(ExportData data) {
                    String country = data.destinationCountry();
                    boolean isEU = country.equals("France") || country.equals("Germany") ||
                            country.equals("Italy") || country.equals("Spain") ||
                            country.equals("United Kingdom");
                    if (!isEU) {
                        return true;
                    }
                    return data.pricePerTon() >= minPrice;
                }

                @Override
                public String fieldName() {
                    return "pricePerTon";
                }

                @Override
                public String errorMessage() {
                    return String.format("EU exports must have price >= %.2f TND/kg", minPrice);
                }

                @Override
                public Object extractValue(ExportData data) {
                    return data.pricePerTon();
                }
            };
        }

        public static ValidationRule highValueMinimumQuantity(double minQuantity) {
            return new ValidationRule() {
                @Override
                public boolean test(ExportData data) {
                    if (!data.productType().isHighValue()) {
                        return true;
                    }
                    return data.volume() >= minQuantity;
                }

                @Override
                public String fieldName() {
                    return "volume";
                }

                @Override
                public String errorMessage() {
                    return String.format("High-value products require minimum %.2f tons", minQuantity);
                }

                @Override
                public Object extractValue(ExportData data) {
                    return data.volume();
                }
            };
        }

        public static ValidationRule recentExportsOnly(int maxDaysOld) {
            return new ValidationRule() {
                @Override
                public boolean test(ExportData data) {
                    LocalDate cutoff = LocalDate.now().minusDays(maxDaysOld);
                    return !data.date().isBefore(cutoff);
                }

                @Override
                public String fieldName() {
                    return "date";
                }

                @Override
                public String errorMessage() {
                    return String.format("Export must be within last %d days", maxDaysOld);
                }

                @Override
                public Object extractValue(ExportData data) {
                    return data.date();
                }
            };
        }
    }
}