package tn.agricultureai;

import tn.agricultureai.domain.model.*;
import tn.agricultureai.service.factory.ServiceFactory;
import tn.agricultureai.service.prediction.DL4JPredictionService;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

/**
 * Demonstration class to show the REAL AI model in action.
 * This proves that we're using actual neural network predictions!
 *
 * @author Your Name
 */
public class RealAIDemo {

    public static void main(String[] args) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("  🤖 REAL AI MODEL DEMONSTRATION");
        System.out.println("  Tunisian Agricultural Export Price Prediction");
        System.out.println("=".repeat(80));

        try {
            // Create and load REAL AI model
            System.out.println("\n📦 Step 1: Creating Real Neural Network...");
            DL4JPredictionService realAI = ServiceFactory.getDL4JPredictionService();

            // Print model information (using the NEW method name)
            System.out.println(realAI.getDetailedModelInfo());

            // Prepare test data
            System.out.println("📊 Step 2: Preparing Test Data...\n");

            List<ExportData> historicalData = generateHistoricalData();
            System.out.println("✅ Generated " + historicalData.size() + " historical export records");

            // Make predictions
            System.out.println("\n🔮 Step 3: Making REAL AI Predictions...\n");
            System.out.println("-".repeat(80));
            System.out.printf("%-20s %-18s %12s %15s %12s%n",
                    "Product", "Destination", "Price", "Confidence", "Deviation");
            System.out.println("-".repeat(80));

            // Test various product-country combinations
            testPrediction(realAI, ProductType.OLIVE_OIL, Country.ITALY, historicalData);
            testPrediction(realAI, ProductType.DATES, Country.FRANCE, historicalData);
            testPrediction(realAI, ProductType.CITRUS, Country.GERMANY, historicalData);
            testPrediction(realAI, ProductType.TOMATOES, Country.SPAIN, historicalData);
            testPrediction(realAI, ProductType.PEPPERS, Country.USA, historicalData);
            testPrediction(realAI, ProductType.SEAFOOD, Country.SAUDI_ARABIA, historicalData);

            System.out.println("-".repeat(80));

            // Compare models
            System.out.println("\n📈 Step 4: Comparing AI Models...\n");
            compareModels(historicalData);

            // Show model capabilities
            System.out.println("\n✨ Step 5: Real AI Capabilities Demonstrated:");
            System.out.println("  ✅ Neural network with 3 layers (6→16→8→1)");
            System.out.println("  ✅ Trained on 500 synthetic samples");
            System.out.println("  ✅ 50 training epochs completed");
            System.out.println("  ✅ Uses ReLU activation functions");
            System.out.println("  ✅ Adam optimizer with learning rate 0.001");
            System.out.println("  ✅ Mean Squared Error (MSE) loss function");
            System.out.println("  ✅ Feature normalization and denormalization");
            System.out.println("  ✅ Real-time inference on new data");

            System.out.println("\n" + "=".repeat(80));
            System.out.println("  🎉 REAL AI MODEL SUCCESSFULLY DEMONSTRATED!");
            System.out.println("=".repeat(80) + "\n");

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Test prediction for a product-country pair
     */
    private static void testPrediction(
            DL4JPredictionService model,
            ProductType product,
            Country country,
            List<ExportData> historicalData
    ) {
        PredictionResult prediction = model.predict(product, country);

        double baselinePrice = product.getAveragePrice();
        double deviation = ((prediction.predictedPrice() - baselinePrice) / baselinePrice) * 100;

        System.out.printf("%-20s %-18s %10.2f € %13.1f%% %11s%.1f%%%n",
                truncate(product.getDisplayName(), 20),
                truncate(country.getName(), 18),
                prediction.predictedPrice(),
                prediction.confidenceScore() * 100,
                deviation >= 0 ? "+" : "",
                deviation
        );
    }

    /**
     * Compare different AI models
     */
    private static void compareModels(List<ExportData> historicalData) {
        System.out.println("Comparing: REAL AI (DL4J) vs Simple Statistical Model");
        System.out.println("-".repeat(80));
        System.out.printf("%-20s %-15s %15s %15s%n",
                "Product", "Real AI Price", "Simple Price", "Difference");
        System.out.println("-".repeat(80));

        DL4JPredictionService realAI = ServiceFactory.getDL4JPredictionService();
        var simpleModel = ServiceFactory.getSimplePredictionService();

        ProductType[] products = {
                ProductType.OLIVE_OIL,
                ProductType.DATES,
                ProductType.CITRUS
        };

        for (ProductType product : products) {
            PredictionResult realPred = realAI.predict(product, Country.FRANCE);
            PredictionResult simplePred = simpleModel.predict(product, Country.FRANCE);

            double difference = realPred.predictedPrice() - simplePred.predictedPrice();

            System.out.printf("%-20s %13.2f € %13.2f € %14s%.2f €%n",
                    truncate(product.getDisplayName(), 20),
                    realPred.predictedPrice(),
                    simplePred.predictedPrice(),
                    difference >= 0 ? "+" : "",
                    difference
            );
        }

        System.out.println("-".repeat(80));
        System.out.println("Note: Real AI model learns patterns from training data,");
        System.out.println("      while simple model uses basic statistical methods.");
    }

    /**
     * Generate synthetic historical data for context
     */
    private static List<ExportData> generateHistoricalData() {
        List<ExportData> data = new ArrayList<>();
        LocalDate startDate = LocalDate.now().minusDays(180);

        for (int i = 0; i < 100; i++) {
            ProductType product = ProductType.values()[i % ProductType.values().length];
            Country country = Country.values()[i % Country.values().length];

            double basePrice = product.getAveragePrice();
            double variation = 0.8 + (Math.random() * 0.4); // ±20%
            double price = basePrice * variation;

            double quantity = 10 + (Math.random() * 80);

            data.add(new ExportData(
                    product,
                    country,
                    quantity,
                    price,
                    startDate.plusDays(i * 2),
                    "Synthetic"
            ));
        }

        return data;
    }

    private static String truncate(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}