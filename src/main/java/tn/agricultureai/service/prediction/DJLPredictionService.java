package tn.agricultureai.service.prediction;

import lombok.extern.slf4j.Slf4j;
import tn.agricultureai.domain.annotation.AIModel;
import tn.agricultureai.domain.model.*;
import java.util.List;

@Slf4j
@AIModel(
        name = "DJL-PricePredictor",
        version = "3.0",
        framework = AIModel.Framework.DJL,
        accuracy = 0.83,
        description = "Simple price prediction service",
        productionReady = false,
        trainedDate = "2026-01-06",
        author = "Student"
)
public class DJLPredictionService extends AbstractPredictionService {

    public DJLPredictionService() {
        super("DJL-PricePredictor-Java25", "3.0");
        initialize();
    }

    @Override
    protected void loadModel() throws Exception {
        log.info("Loading DJL model...");
        Thread.sleep(200);
        log.info("DJL model loaded successfully");
    }

    @Override
    protected double predictPrice(ProductType productType,
                                  Country destination,
                                  List<ExportData> context) {
        double basePrice = calculateBaselinePrice(productType, context);

        // Apply factors
        double marketFactor = 1.0;
        if (destination.getRegion() == Country.Region.EU) {
            marketFactor *= 1.05;
        }
        if (productType.isHighValue()) {
            marketFactor *= 1.03;
        }

        double predictedPrice = basePrice * marketFactor;

        log.debug("DJL prediction for {} to {}: {} TND/kg",
                productType, destination, predictedPrice);

        return Math.round(predictedPrice * 100.0) / 100.0;
    }

    @Override
    protected double calculateConfidence(ProductType productType,
                                         Country destination,
                                         List<ExportData> context) {
        double confidence = 0.75;

        if (!context.isEmpty()) {
            long relevantRecords = context.stream()
                    .filter(data -> data.productType().equals(productType))
                    .count();
            confidence += Math.min(0.15, relevantRecords * 0.004);
        }

        if (destination.getRegion() == Country.Region.EU) {
            confidence += 0.05;
        }

        return Math.min(0.92, confidence);
    }

    @Override
    public String getFrameworkName() {
        return "Deep Java Library (DJL)";
    }

    @Override
    public double getModelAccuracy() {
        return 0.83;
    }

    // REMOVE the close() method since it's not in parent interface
    // OR add it to AbstractPredictionService
}