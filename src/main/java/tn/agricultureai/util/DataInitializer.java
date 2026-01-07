package tn.agricultureai.util;

import tn.agricultureai.domain.model.*;
import tn.agricultureai.repository.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public final class DataInitializer {

    private static final Random RANDOM = new Random(42);

    private DataInitializer() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void initializeExportData(ExportDataRepository repository) {
        List<ExportData> exports = new ArrayList<>();

        LocalDate startDate = LocalDate.now().minusDays(90);

        for (int i = 0; i < 100; i++) {
            ProductType product = randomProductType();
            String country = randomCountry().getName();  // Get country name string
            LocalDate exportDate = startDate.plusDays(RANDOM.nextInt(90));

            double basePrice = product.getAveragePrice();
            double variation = 0.8 + (RANDOM.nextDouble() * 0.4);
            double price = basePrice * variation;

            double quantity = 5 + (RANDOM.nextDouble() * 50);

            // FIXED LINE 44: Correct constructor parameter order
            ExportData export = new ExportData(
                    exportDate,      // LocalDate date
                    product,         // ProductType productType
                    price,           // double pricePerTon
                    quantity,        // double volume
                    country,         // String destinationCountry
                    randomMarketIndicator()  // MarketIndicator indicator
            );

            exports.add(export);
        }

        repository.saveAll(exports);
        System.out.println("✅ Initialized " + exports.size() + " export records");
    }

    public static void initializePredictions(PredictionRepository repository) {
        List<PredictionResult> predictions = new ArrayList<>();

        for (ProductType product : ProductType.values()) {
            // Get first 5 countries
            Country[] countries = Country.values();
            int limit = Math.min(5, countries.length);

            for (int j = 0; j < limit; j++) {
                Country country = countries[j];
                double basePrice = product.getAveragePrice();
                double variation = 0.85 + (RANDOM.nextDouble() * 0.3);
                double predictedPrice = basePrice * variation;

                double confidence = 0.6 + (RANDOM.nextDouble() * 0.35);

                PredictionResult prediction = new PredictionResult(
                        product,
                        country,
                        predictedPrice,
                        confidence,
                        null,
                        LocalDateTime.now().minusHours(RANDOM.nextInt(48)),
                        randomModel(),
                        "{}"
                );

                predictions.add(prediction);
            }
        }

        repository.saveAll(predictions);
        System.out.println("✅ Initialized " + predictions.size() + " predictions");
    }

    public static void initializeReports(ReportRepository repository) {
        List<MarketReport> reports = new ArrayList<>();

        MarketReport.ReportType[] types = MarketReport.ReportType.values();

        for (int i = 0; i < 20; i++) {
            MarketReport.ReportType type = types[RANDOM.nextInt(types.length)];

            String title = generateReportTitle(type);
            String content = generateReportContent(type);

            List<PredictionResult> predictions = new ArrayList<>();
            int predCount = 2 + RANDOM.nextInt(5);

            for (int j = 0; j < predCount; j++) {
                predictions.add(new PredictionResult(
                        randomProductType(),
                        randomCountry(),
                        8.0 + RANDOM.nextDouble() * 10.0,
                        0.7 + RANDOM.nextDouble() * 0.25
                ));
            }

            MarketReport report = new MarketReport(
                    title,
                    content,
                    type,
                    predictions
            );

            reports.add(report);
        }

        repository.saveAll(reports);
        System.out.println("✅ Initialized " + reports.size() + " reports");
    }

    public static void initializeAllData(
            ExportDataRepository exportRepo,
            PredictionRepository predictionRepo,
            ReportRepository reportRepo
    ) {
        System.out.println("\n--- Initializing Sample Data ---");
        initializeExportData(exportRepo);
        initializePredictions(predictionRepo);
        initializeReports(reportRepo);
        System.out.println("✅ All data initialized successfully!\n");
    }

    // Helper methods
    private static ProductType randomProductType() {
        ProductType[] types = ProductType.values();
        return types[RANDOM.nextInt(types.length)];
    }

    private static Country randomCountry() {
        Country[] countries = Country.values();
        return countries[RANDOM.nextInt(countries.length)];
    }

    private static MarketIndicator randomMarketIndicator() {
        MarketIndicator[] indicators = MarketIndicator.values();
        return indicators[RANDOM.nextInt(indicators.length)];
    }

    private static String randomModel() {
        String[] models = {"DJL-Predictor-v1", "ONNX-PriceModel-v2", "TF-AgriPredict"};
        return models[RANDOM.nextInt(models.length)];
    }

    private static String generateReportTitle(MarketReport.ReportType type) {
        return switch (type) {
            case DAILY_SUMMARY -> "Daily Market Summary - " + LocalDate.now();
            case WEEKLY_ANALYSIS -> "Weekly Analysis: Week " + LocalDate.now().getDayOfYear() / 7;
            case PRODUCT_FOCUS -> "Product Focus: " + randomProductType().getDisplayName();
            case COUNTRY_FOCUS -> "Country Analysis: " + randomCountry().getName();
            case TREND_ANALYSIS -> "Market Trends for Q" + ((LocalDate.now().getMonthValue() - 1) / 3 + 1);
            case CUSTOM -> "Custom Report #" + RANDOM.nextInt(1000);
        };
    }

    private static String generateReportContent(MarketReport.ReportType type) {
        return switch (type) {
            case DAILY_SUMMARY ->
                    "Today's market shows stable prices across most agricultural exports. " +
                            "Olive oil maintains strong demand from European markets.";
            case WEEKLY_ANALYSIS ->
                    "This week saw a 3% increase in date exports to Middle Eastern markets. " +
                            "Citrus fruits prices remain competitive due to favorable weather conditions.";
            case PRODUCT_FOCUS ->
                    "Analysis of " + randomProductType().getDisplayName() + " exports reveals " +
                            "strong performance in Q4. Market conditions are favorable for expansion.";
            case COUNTRY_FOCUS ->
                    randomCountry().getName() + " continues to be a key export destination. " +
                            "Trade volumes have increased by 8% compared to last quarter.";
            case TREND_ANALYSIS ->
                    "Long-term trends indicate growing demand for Tunisian agricultural products. " +
                            "Sustainability certifications are becoming increasingly important.";
            case CUSTOM ->
                    "Comprehensive analysis of current market conditions and future projections. " +
                            "Multiple factors influencing export dynamics are examined in detail.";
        };
    }
}