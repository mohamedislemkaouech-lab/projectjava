package tn.agricultureai.service.prediction;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tn.agricultureai.domain.exception.PredictionException;
import tn.agricultureai.domain.model.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract base class for prediction services.
 * Demonstrates: Abstract class, template method pattern, Lombok.
 *
 * @author Your Name
 */
@Slf4j  // Lombok: Auto-generates logger
@Getter // Lombok: Auto-generates getters
public abstract class AbstractPredictionService implements PredictionService {

    protected final String modelName;
    protected final String version;
    protected boolean ready;

    /**
     * Constructor
     */
    protected AbstractPredictionService(String modelName, String version) {
        this.modelName = modelName;
        this.version = version;
        this.ready = false;
    }

    /**
     * Initialize the model (called by subclasses)
     * Template method pattern
     */
    protected void initialize() {
        log.info("Initializing prediction service: {}", modelName);
        try {
            loadModel();
            this.ready = true;
            log.info("Model {} loaded successfully", modelName);
        } catch (Exception e) {
            log.error("Failed to load model: {}", modelName, e);
            throw PredictionException.modelLoadFailure(modelName, e);
        }
    }

    /**
     * Abstract method - subclasses must implement model loading
     */
    protected abstract void loadModel() throws Exception;

    /**
     * Abstract method - core prediction logic
     */
    protected abstract double predictPrice(
            ProductType productType,
            Country destination,
            List<ExportData> context
    );

    /**
     * Abstract method - calculate confidence score
     */
    protected abstract double calculateConfidence(
            ProductType productType,
            Country destination,
            List<ExportData> context
    );

    @Override
    public PredictionResult predict(ProductType productType, Country destination) {
        validateReady();
        validateInputs(productType, destination);

        log.debug("Predicting price for {} to {}", productType, destination);

        try {
            double predictedPrice = predictPrice(productType, destination, List.of());
            double confidence = calculateConfidence(productType, destination, List.of());

            return new PredictionResult(
                    productType,
                    destination,
                    predictedPrice,
                    confidence,
                    null,
                    null,
                    modelName,
                    null
            );
        } catch (Exception e) {
            log.error("Prediction failed", e);
            throw new PredictionException(
                    "Prediction failed: " + e.getMessage(),
                    productType,
                    destination
            );
        }
    }

    @Override
    public List<PredictionResult> predictBatch(
            List<ProductType> productTypes,
            Country destination
    ) {
        validateReady();

        log.debug("Batch prediction for {} products to {}",
                productTypes.size(), destination);

        return productTypes.stream()
                .map(product -> predict(product, destination))
                .collect(Collectors.toList());
    }

    @Override
    public PredictionResult predictWithContext(
            ProductType productType,
            Country destination,
            List<ExportData> historicalData
    ) {
        validateReady();
        validateInputs(productType, destination);

        log.debug("Predicting with {} historical records", historicalData.size());

        try {
            double predictedPrice = predictPrice(productType, destination, historicalData);
            double confidence = calculateConfidence(productType, destination, historicalData);

            return new PredictionResult(
                    productType,
                    destination,
                    predictedPrice,
                    confidence,
                    null,
                    null,
                    modelName,
                    String.format("{\"historicalRecords\":%d}", historicalData.size())
            );
        } catch (Exception e) {
            log.error("Context-based prediction failed", e);
            throw new PredictionException(
                    "Prediction with context failed: " + e.getMessage(),
                    productType,
                    destination
            );
        }
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    @Override
    public ModelInfo getModelInfo() {
        return new ModelInfo(
                modelName,
                version,
                getFrameworkName(),
                getModelAccuracy(),
                ready
        );
    }

    /**
     * Get framework name (implemented by subclasses)
     */
    protected abstract String getFrameworkName();

    /**
     * Get model accuracy (implemented by subclasses)
     */
    protected abstract double getModelAccuracy();

    /**
     * Validate model is ready
     */
    protected void validateReady() {
        if (!ready) {
            throw PredictionException.modelLoadFailure(
                    modelName,
                    new IllegalStateException("Model not initialized")
            );
        }
    }

    /**
     * Validate inputs
     */
    protected void validateInputs(ProductType productType, Country destination) {
        if (productType == null) {
            throw new IllegalArgumentException("Product type cannot be null");
        }
        if (destination == null) {
            throw new IllegalArgumentException("Destination cannot be null");
        }
    }

    /**
     * Calculate baseline price from historical data
     */
    protected double calculateBaselinePrice(
            ProductType productType,
            List<ExportData> historicalData
    ) {
        if (historicalData.isEmpty()) {
            return productType.getAveragePrice();
        }

        return historicalData.stream()
                .filter(data -> data.productType().equals(productType))
                .mapToDouble(ExportData::pricePerUnit)
                .average()
                .orElse(productType.getAveragePrice());
    }
}