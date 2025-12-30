package tn.agricultureai.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Record representing AI prediction result.
 * Demonstrates: Record with computed fields, multiple constructors.
 */
public record PredictionResult(
        ProductType productType,
        Country destination,
        double predictedPrice,      // EUR per kg
        double confidenceScore,     // 0.0 to 1.0
        ConfidenceLevel confidenceLevel,
        LocalDateTime predictionTime,
        String modelName,
        String metadata             // JSON or additional info
) {
    /**
     * Compact constructor with validation and auto-computation
     */
    public PredictionResult {
        Objects.requireNonNull(productType, "Product type cannot be null");
        Objects.requireNonNull(destination, "Destination cannot be null");

        if (predictedPrice <= 0) {
            throw new IllegalArgumentException("Predicted price must be positive");
        }

        if (confidenceScore < 0.0 || confidenceScore > 1.0) {
            throw new IllegalArgumentException(
                    "Confidence score must be between 0.0 and 1.0"
            );
        }

        // Auto-compute confidence level if not provided
        if (confidenceLevel == null) {
            confidenceLevel = ConfidenceLevel.fromScore(confidenceScore);
        }

        // Default prediction time to now if not provided
        if (predictionTime == null) {
            predictionTime = LocalDateTime.now();
        }

        // Default model name if not provided
        if (modelName == null || modelName.isBlank()) {
            modelName = "DefaultModel";
        }

        // Default metadata
        if (metadata == null) {
            metadata = "{}";
        }
    }

    /**
     * Simplified constructor (most common use case)
     */
    public PredictionResult(
            ProductType productType,
            Country destination,
            double predictedPrice,
            double confidenceScore
    ) {
        this(
                productType,
                destination,
                predictedPrice,
                confidenceScore,
                null,  // Will be auto-computed
                null,  // Will be set to now
                null,  // Will use default
                null   // Will use default
        );
    }

    /**
     * Check if prediction is reliable (confidence >= MEDIUM)
     */
    public boolean isReliable() {
        return confidenceLevel.isAcceptable();
    }

    /**
     * Get price deviation from historical average (%)
     */
    public double getPriceDeviation() {
        double avgPrice = productType.getAveragePrice();
        return ((predictedPrice - avgPrice) / avgPrice) * 100;
    }

    /**
     * Get formatted prediction summary
     */
    public String getSummary() {
        return String.format(
                "%s to %s: %.2f EUR/kg (Confidence: %s, Deviation: %+.1f%%)",
                productType.getDisplayName(),
                destination.getName(),
                predictedPrice,
                confidenceLevel.getLabel(),
                getPriceDeviation()
        );
    }

    /**
     * Compare with actual export data (for validation)
     */
    public double calculateError(ExportData actualData) {
        if (!actualData.productType().equals(this.productType) ||
                !actualData.destination().equals(this.destination)) {
            throw new IllegalArgumentException("Data mismatch: different product or destination");
        }

        return Math.abs(predictedPrice - actualData.pricePerUnit());
    }
}