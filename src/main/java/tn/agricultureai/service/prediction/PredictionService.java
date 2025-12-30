package tn.agricultureai.service.prediction;

import tn.agricultureai.domain.model.*;
import java.util.List;

/**
 * Interface for prediction services.
 * Demonstrates: Interface design, polymorphism.
 *
 * @author Your Name
 */
public interface PredictionService {

    /**
     * Predict export price for a product-destination pair
     *
     * @param productType Product to predict
     * @param destination Destination country
     * @return Prediction result with confidence
     */
    PredictionResult predict(ProductType productType, Country destination);

    /**
     * Batch prediction for multiple products
     *
     * @param productTypes List of products
     * @param destination Destination country
     * @return List of predictions
     */
    List<PredictionResult> predictBatch(List<ProductType> productTypes, Country destination);

    /**
     * Predict with historical data context
     *
     * @param productType Product type
     * @param destination Destination
     * @param historicalData Historical export data for context
     * @return Prediction result
     */
    PredictionResult predictWithContext(
            ProductType productType,
            Country destination,
            List<ExportData> historicalData
    );

    /**
     * Get model name/identifier
     *
     * @return Model name
     */
    String getModelName();

    /**
     * Check if model is ready/loaded
     *
     * @return true if ready
     */
    boolean isReady();

    /**
     * Get model metadata
     *
     * @return Model information
     */
    ModelInfo getModelInfo();

    /**
     * Record for model metadata
     */
    record ModelInfo(
            String name,
            String version,
            String framework,
            double accuracy,
            boolean loaded
    ) {}
}