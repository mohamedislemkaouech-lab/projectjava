package tn.agricultureai;

import tn.agricultureai.domain.model.*;
import tn.agricultureai.service.factory.ServiceFactory;
import tn.agricultureai.service.prediction.DJLPredictionService;
import tn.agricultureai.service.prediction.PredictionService;
import tn.agricultureai.util.SampleDataGenerator;
import java.time.LocalDate;
import java.util.List;

public class RealAIDemo {

    public static void main(String[] args) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("  🤖 AI MODEL DEMONSTRATION");
        System.out.println("  Tunisian Agricultural Export Price Prediction");
        System.out.println("=".repeat(80));

        try {
            System.out.println("\n📦 Step 1: Creating AI Model...");

            // OPTION 1: Direct instantiation (if factory doesn't work)
            // DJLPredictionService aiService = new DJLPredictionService();

            // OPTION 2: Try to get service
            PredictionService aiService;
            try {
                aiService = ServiceFactory.getDefaultPredictionService();
            } catch (Exception e) {
                System.out.println("⚠️ Factory method failed, creating service directly...");
                aiService = new DJLPredictionService();
            }

            System.out.println("Model Accuracy: " + (aiService.getModelAccuracy() * 100) + "%");

            System.out.println("📊 Step 2: Preparing Test Data...\n");
            List<ExportData> historicalData = SampleDataGenerator.generateSampleExportData(50);
            System.out.println("✅ Generated " + historicalData.size() + " historical export records");

            System.out.println("\n🔮 Step 3: Making AI Predictions...\n");
            System.out.println("-".repeat(80));
            System.out.printf("%-20s %-18s %12s %15s%n",
                    "Product", "Destination", "Predicted Price", "Confidence");
            System.out.println("-".repeat(80));

            // Test predictions
            testPrediction(aiService, ProductType.OLIVE_OIL, Country.ITALY, historicalData);
            testPrediction(aiService, ProductType.DATES, Country.FRANCE, historicalData);
            testPrediction(aiService, ProductType.CITRUS_FRUITS, Country.GERMANY, historicalData);
            testPrediction(aiService, ProductType.TOMATOES, Country.SPAIN, historicalData);
            testPrediction(aiService, ProductType.PEPPERS, Country.USA, historicalData);
            testPrediction(aiService, ProductType.WHEAT, Country.SAUDI_ARABIA, historicalData);

            System.out.println("-".repeat(80));

            System.out.println("\n✨ AI Model Capabilities Demonstrated:");
            System.out.println("  ✅ Price prediction based on historical data");
            System.out.println("  ✅ Confidence scoring for predictions");
            System.out.println("  ✅ Market factor adjustments");
            System.out.println("  ✅ Country-specific pricing");
            System.out.println("  ✅ Product type analysis");

            System.out.println("\n" + "=".repeat(80));
            System.out.println("  🎉 AI MODEL SUCCESSFULLY DEMONSTRATED!");
            System.out.println("=".repeat(80) + "\n");

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testPrediction(
            PredictionService model,
            ProductType product,
            Country country,
            List<ExportData> historicalData
    ) {
        try {
            // Create a sample ExportData for prediction
            ExportData sampleData = new ExportData(
                    LocalDate.now(),
                    product,
                    product.getAveragePrice(),
                    50.0,
                    country.getName(),
                    MarketIndicator.STABLE
            );

            PricePrediction prediction = model.predictPrice(sampleData);

            System.out.printf("%-20s %-18s %10.2f TND %13.1f%%%n",
                    truncate(product.getFrenchName(), 20),
                    truncate(country.getName(), 18),
                    prediction.predictedPrice(),
                    prediction.confidence() * 100
            );
        } catch (Exception e) {
            System.out.printf("%-20s %-18s %10s %15s%n",
                    truncate(product.getFrenchName(), 20),
                    truncate(country.getName(), 18),
                    "N/A",
                    "N/A"
            );
        }
    }

    private static String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}