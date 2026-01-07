package tn.agricultureai.domain.model;

import java.time.LocalDate;

public record PricePrediction(
        LocalDate predictionDate,
        ProductType productType,
        double predictedPrice,
        double confidence,
        String modelName,
        PredictionStatus status
) {
    public PricePrediction {
        if (predictedPrice < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (confidence < 0 || confidence > 1) {
            throw new IllegalArgumentException("Confidence must be between 0 and 1");
        }
    }
}