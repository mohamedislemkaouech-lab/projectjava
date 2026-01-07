package tn.agricultureai.service.prediction;

import tn.agricultureai.domain.model.ExportData;
import tn.agricultureai.domain.model.PricePrediction;
import tn.agricultureai.domain.model.ProductType;
import tn.agricultureai.domain.model.Country;
import java.util.List;
import java.util.Optional;

/**
 * Interface for price prediction services.
 * Demonstrates: Service interface design, AI model abstraction.
 *
 * @author Your Name
 */
public interface PredictionService {

    /**
     * Predict price for a single export data record
     *
     * @param input Export data containing product, destination, historical data
     * @return Price prediction with confidence and metadata
     */
    PricePrediction predictPrice(ExportData input);

    /**
     * Predict prices for multiple export data records
     *
     * @param inputs List of export data records
     * @return List of price predictions
     */
    List<PricePrediction> predictBatch(List<ExportData> inputs);

    /**
     * Get the model's accuracy score (0.0 to 1.0)
     *
     * @return Model accuracy percentage as decimal
     */
    double getModelAccuracy();

    /**
     * Get the model's name/identifier
     *
     * @return Model name
     */
    default String getModelName() {
        return "Unknown-Model";
    }

    /**
     * Get the model's version
     *
     * @return Model version
     */
    default String getModelVersion() {
        return "1.0";
    }

    /**
     * Get the AI framework name
     *
     * @return Framework name (DJL, TensorFlow, ONNX, etc.)
     */
    default String getFrameworkName() {
        return "Unknown-Framework";
    }

    /**
     * Check if model is loaded and ready
     *
     * @return true if model is ready for predictions
     */
    default boolean isModelReady() {
        return true;
    }

    /**
     * Get model description
     *
     * @return Model description
     */
    default String getModelDescription() {
        return "AI model for price prediction";
    }

    /**
     * Load the model (if needed)
     *
     * @throws Exception if model loading fails
     */
    default void loadModel() throws Exception {
        // Default implementation does nothing
    }

    /**
     * Unload the model (if needed)
     */
    default void unloadModel() {
        // Default implementation does nothing
    }

    /**
     * Validate if input data is suitable for prediction
     *
     * @param input Export data to validate
     * @return true if data is valid for prediction
     */
    default boolean validateInput(ExportData input) {
        return input != null
                && input.productType() != null
                && input.destinationCountry() != null
                && input.pricePerTon() > 0
                && input.volume() > 0;
    }

    /**
     * Get prediction for specific product and destination
     *
     * @param productType Type of product
     * @param destination Destination country
     * @param context Historical data for context (optional)
     * @return Price prediction
     */
    default PricePrediction predictForProductAndDestination(
            ProductType productType,
            Country destination,
            List<ExportData> context
    ) {
        // Create a sample ExportData for the prediction
        ExportData sampleData = new ExportData(
                java.time.LocalDate.now(),
                productType,
                productType.getAveragePrice(),
                50.0,
                destination.getName(),
                tn.agricultureai.domain.model.MarketIndicator.STABLE
        );

        return predictPrice(sampleData);
    }

    /**
     * Get model statistics
     *
     * @return Model statistics as string
     */
    default String getModelStatistics() {
        return String.format(
                "Model: %s v%s | Accuracy: %.1f%% | Framework: %s",
                getModelName(),
                getModelVersion(),
                getModelAccuracy() * 100,
                getFrameworkName()
        );
    }

    /**
     * Test the model with sample data
     *
     * @return Test results
     */
    default String testModel() {
        StringBuilder result = new StringBuilder();
        result.append("=== Model Test ===\n");
        result.append(getModelStatistics()).append("\n");
        result.append("Model ready: ").append(isModelReady()).append("\n");
        result.append("==================\n");
        return result.toString();
    }

    /**
     * Compare predictions from different models
     *
     * @param otherService Another prediction service to compare with
     * @param testData Test data for comparison
     * @return Comparison results
     */
    default String compareWith(PredictionService otherService, List<ExportData> testData) {
        if (testData.isEmpty()) {
            return "No test data provided for comparison";
        }

        double thisTotal = 0.0;
        double otherTotal = 0.0;
        int validPredictions = 0;

        for (ExportData data : testData) {
            try {
                PricePrediction myPrediction = predictPrice(data);
                PricePrediction otherPrediction = otherService.predictPrice(data);

                thisTotal += myPrediction.predictedPrice();
                otherTotal += otherPrediction.predictedPrice();
                validPredictions++;
            } catch (Exception e) {
                // Skip failed predictions
            }
        }

        if (validPredictions == 0) {
            return "No valid predictions for comparison";
        }

        double thisAvg = thisTotal / validPredictions;
        double otherAvg = otherTotal / validPredictions;
        double difference = Math.abs(thisAvg - otherAvg);
        double differencePercent = (difference / Math.min(thisAvg, otherAvg)) * 100;

        return String.format(
                "Model Comparison:\n" +
                        "  %s: Average prediction = %.2f\n" +
                        "  %s: Average prediction = %.2f\n" +
                        "  Difference: %.2f (%.1f%%)\n" +
                        "  Based on %d valid predictions",
                getModelName(), thisAvg,
                otherService.getModelName(), otherAvg,
                difference, differencePercent,
                validPredictions
        );
    }

    /**
     * Calculate prediction confidence based on input quality
     *
     * @param input Export data
     * @return Confidence score (0.0 to 1.0)
     */
    default double calculateConfidence(ExportData input) {
        double confidence = 0.5; // Base confidence

        // Increase confidence for complete data
        if (input.productType() != null) {
            confidence += 0.1;
        }

        if (input.destinationCountry() != null && !input.destinationCountry().isEmpty()) {
            confidence += 0.1;
        }

        if (input.pricePerTon() > 0) {
            confidence += 0.1;
        }

        if (input.volume() > 0) {
            confidence += 0.1;
        }

        if (input.date() != null) {
            confidence += 0.1;
        }

        return Math.min(0.95, confidence);
    }

    /**
     * Get model capabilities description
     *
     * @return Capabilities description
     */
    default String getCapabilities() {
        return """
            This prediction service provides:
            • Price forecasting for agricultural exports
            • Confidence scoring for predictions
            • Batch prediction processing
            • Input data validation
            • Model comparison functionality
            """;
    }

    /**
     * Functional interface for prediction callbacks
     */
    @FunctionalInterface
    interface PredictionCallback {
        void onPredictionComplete(PricePrediction prediction, Exception error);
    }

    /**
     * Async prediction with callback
     *
     * @param input Export data
     * @param callback Callback to handle result
     */
    default void predictPriceAsync(ExportData input, PredictionCallback callback) {
        new Thread(() -> {
            try {
                PricePrediction prediction = predictPrice(input);
                callback.onPredictionComplete(prediction, null);
            } catch (Exception e) {
                callback.onPredictionComplete(null, e);
            }
        }).start();
    }
}