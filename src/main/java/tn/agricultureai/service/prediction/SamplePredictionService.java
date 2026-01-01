package tn.agricultureai.service.prediction;

import tn.agricultureai.domain.model.*;
import java.util.*;

public class SamplePredictionService implements PredictionService {

    @Override
    public String getModelName() {
        return "Simple-Predictor-v1";
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public ModelInfo getModelInfo() {
        return new ModelInfo(
                "Simple Prediction Model",
                "1.0",
                "Custom",
                0.78,
                true
        );
    }

    @Override
    public PredictionResult predict(ProductType productType, Country destination) {
        if (productType == null || destination == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }

        double basePrice = productType.getAveragePrice();
        double predictedPrice = basePrice * (0.9 + Math.random() * 0.2);
        double confidence = 0.7 + (Math.random() * 0.2);

        return new PredictionResult(
                productType,
                destination,
                predictedPrice,
                confidence
        );
    }

    @Override
    public List<PredictionResult> predictBatch(List<ProductType> productTypes, Country destination) {
        List<PredictionResult> results = new ArrayList<>();
        for (ProductType productType : productTypes) {
            results.add(predict(productType, destination));
        }
        return results;
    }

    @Override
    public PredictionResult predictWithContext(
            ProductType productType,
            Country destination,
            List<ExportData> historicalData
    ) {
        if (historicalData == null || historicalData.isEmpty()) {
            return predict(productType, destination);
        }

        double avgHistoricalPrice = historicalData.stream()
                .mapToDouble(ExportData::pricePerUnit)
                .average()
                .orElse(productType.getAveragePrice());

        return new PredictionResult(
                productType,
                destination,
                avgHistoricalPrice * (0.95 + Math.random() * 0.1),
                0.8
        );
    }
}