// File: src/main/java/tn/isg/economics/util/SampleDataGenerator.java
package tn.agricultureai.util;

import tn.agricultureai.domain.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SampleDataGenerator {

    public static List<ExportData> generateSampleExportData(int count) {
        List<ExportData> sampleData = new ArrayList<>();
        Random random = new Random(42);
        LocalDate baseDate = LocalDate.now().minusYears(2);

        ProductType[] products = ProductType.values();
        String[] countries = {"France", "Germany", "Italy", "Spain", "United Kingdom"};
        MarketIndicator[] indicators = MarketIndicator.values();

        for (int i = 0; i < count; i++) {
            ProductType product = products[random.nextInt(products.length)];
            LocalDate date = baseDate.plusDays(random.nextInt(730)); // Within 2 years
            double price = getBasePrice(product) * (0.8 + random.nextDouble() * 0.4); // ±20%
            double volume = getBaseVolume(product) * (0.5 + random.nextDouble()); // 50-150%
            String country = countries[random.nextInt(countries.length)];
            MarketIndicator indicator = indicators[random.nextInt(indicators.length)];

            sampleData.add(new ExportData(date, product, price, volume, country, indicator));
        }

        return sampleData;
    }

    private static double getBasePrice(ProductType product) {
        switch (product) {
            case OLIVE_OIL: return 3200.0;
            case DATES: return 2200.0;
            case CITRUS_FRUITS: return 1500.0;
            case WHEAT: return 800.0;
            case TOMATOES: return 1200.0;
            case PEPPERS: return 1800.0;
            default: return 1000.0;
        }
    }

    private static double getBaseVolume(ProductType product) {
        switch (product) {
            case OLIVE_OIL: return 50.0;
            case DATES: return 30.0;
            case CITRUS_FRUITS: return 100.0;
            case WHEAT: return 200.0;
            case TOMATOES: return 80.0;
            case PEPPERS: return 35.0;
            default: return 50.0;
        }
    }

    public static List<PricePrediction> generateSamplePredictions(int count) {
        List<PricePrediction> predictions = new ArrayList<>();
        Random random = new Random(42);
        LocalDate baseDate = LocalDate.now();

        ProductType[] products = ProductType.values();
        String[] models = {"DJL-LSTM", "ONNX-Regression", "TensorFlow-NN"};
        PredictionStatus[] statuses = PredictionStatus.values();

        for (int i = 0; i < count; i++) {
            ProductType product = products[random.nextInt(products.length)];
            LocalDate date = baseDate.plusDays(random.nextInt(90)); // Next 90 days
            double predictedPrice = getBasePrice(product) * (0.85 + random.nextDouble() * 0.3);
            double confidence = 0.6 + random.nextDouble() * 0.35; // 60-95%
            String model = models[random.nextInt(models.length)];
            PredictionStatus status = confidence > 0.7 ? PredictionStatus.COMPLETED :
                    random.nextDouble() > 0.8 ? PredictionStatus.FAILED :
                            PredictionStatus.LOW_CONFIDENCE;

            predictions.add(new PricePrediction(
                    date, product, predictedPrice, confidence, model, status
            ));
        }

        return predictions;
    }
}