package tn.agricultureai;

import tn.agricultureai.domain.model.ExportData;
import tn.agricultureai.domain.model.PricePrediction;
import tn.agricultureai.util.SampleDataGenerator;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Tunisian Agricultural Export Intelligence System ===\n");

        // Generate sample data
        List<ExportData> exports = SampleDataGenerator.generateSampleExportData(10);
        System.out.println("Generated " + exports.size() + " export records:");

        for (ExportData export : exports) {
            System.out.printf("- %s: %.2f TND/ton, %.1f tons to %s%n",
                    export.productType().getFrenchName(),
                    export.pricePerTon(),
                    export.volume(),
                    export.destinationCountry());
        }

        // Generate predictions
        List<PricePrediction> predictions = SampleDataGenerator.generateSamplePredictions(5);
        System.out.println("\nGenerated " + predictions.size() + " price predictions:");

        for (PricePrediction prediction : predictions) {
            System.out.printf("- %s: %.2f TND (%.1f%% confidence) - %s%n",
                    prediction.productType().getFrenchName(),
                    prediction.predictedPrice(),
                    prediction.confidence() * 100,
                    prediction.status());
        }

        System.out.println("\n✅ System is working!");
    }
}