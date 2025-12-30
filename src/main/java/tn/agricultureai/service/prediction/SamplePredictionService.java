package tn.agricultureai.service.prediction;

import tn.agricultureai.domain.annotation.*;
import tn.agricultureai.domain.model.*;
import java.util.concurrent.TimeUnit;

/**
 * Sample service demonstrating annotation usage.
 * This is for demonstration purposes in Step 4.
 *
 * @author Your Name
 */
@AIModel(
        name = "TunisianExportPricePredictor",
        version = "2.1",
        framework = AIModel.Framework.ONNX,
        accuracy = 0.87,
        description = "Predicts export prices for Tunisian agricultural products",
        productionReady = true,
        trainedDate = "2024-12-01",
        author = "Agriculture AI Team"
)
public class SamplePredictionService {

    @Validated(
            notNull = true,
            min = 0.0,
            max = 1.0,
            message = "Confidence must be between 0.0 and 1.0"
    )
    private double confidenceThreshold = 0.75;

    @Validated(
            required = true,
            notEmpty = true,
            pattern = "^[A-Z0-9-]+$",
            message = "Model ID must contain only uppercase letters, numbers, and hyphens"
    )
    private String modelId = "PRED-001";

    @Validated(
            min = 1,
            max = 1000,
            message = "Batch size must be between 1 and 1000"
    )
    private int batchSize = 100;

    /**
     * Predict price with caching enabled
     */
    @Cacheable(
            key = "prediction",
            ttl = 1,
            unit = TimeUnit.HOURS,
            strategy = Cacheable.Strategy.LRU,
            maxSize = 500,
            condition = "result.isReliable()",
            region = "predictions",
            priority = Cacheable.Priority.HIGH
    )
    public PredictionResult predict(
            @Validated(notNull = true) ProductType productType,
            @Validated(notNull = true) Country destination
    ) {
        // Simulated prediction logic
        double basePrice = productType.getAveragePrice();
        double randomFactor = 0.9 + (Math.random() * 0.2); // ±10%
        double predictedPrice = basePrice * randomFactor;

        return new PredictionResult(
                productType,
                destination,
                predictedPrice,
                0.85
        );
    }

    /**
     * Batch prediction with different cache settings
     */
    @Cacheable(
            key = "batchPrediction",
            ttl = 30,
            unit = TimeUnit.MINUTES,
            strategy = Cacheable.Strategy.LFU,
            maxSize = 100,
            region = "batch-predictions"
    )
    public java.util.List<PredictionResult> predictBatch(
            @Validated(notEmpty = true) java.util.List<ProductType> products,
            @Validated(notNull = true) Country destination
    ) {
        return products.stream()
                .map(product -> predict(product, destination))
                .toList();
    }

    /**
     * Non-cached method for comparison
     */
    public PredictionResult predictRealtime(ProductType productType, Country destination) {
        return predict(productType, destination);
    }

    // Getters and setters
    public double getConfidenceThreshold() {
        return confidenceThreshold;
    }

    public void setConfidenceThreshold(double confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}