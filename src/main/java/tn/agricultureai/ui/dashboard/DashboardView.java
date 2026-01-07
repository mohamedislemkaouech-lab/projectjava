package tn.agricultureai.ui.dashboard;

import tn.agricultureai.domain.model.*;
import tn.agricultureai.ui.chart.ChartStrategy;
import java.util.*;

public class DashboardView {

    private ChartStrategy chartStrategy;

    public void displayMainMenu() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("  🌾 TUNISIAN AGRICULTURAL EXPORT INTELLIGENCE DASHBOARD 🌾");
        System.out.println("=".repeat(70));
        System.out.println("\n📊 MAIN MENU:");
        System.out.println("  1. View Statistics");
        System.out.println("  2. Generate Predictions");
        System.out.println("  3. View Export Data");
        System.out.println("  4. Generate Market Report");
        System.out.println("  5. Display Charts");
        System.out.println("  6. Search & Filter");
        System.out.println("  0. Exit");
        System.out.println("\n" + "─".repeat(70));
        System.out.print("Enter your choice: ");
    }

    public void displayStatistics(DashboardModel.DashboardStatistics stats) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("  📊 DASHBOARD STATISTICS");
        System.out.println("=".repeat(70));
        System.out.println();
        System.out.printf("  Total Export Records:        %,d%n", stats.totalExports());
        System.out.printf("  Total Predictions:           %,d%n", stats.totalPredictions());
        System.out.printf("  Total Reports:               %,d%n", stats.totalReports());
        System.out.printf("  Average Export Price:        %.2f EUR/kg%n", stats.averageExportPrice());
        System.out.printf("  Average Prediction Confidence: %.1f%%%n", stats.averagePredictionConfidence() * 100);
        System.out.println("=".repeat(70));
    }

    public void displayPredictions(List<PredictionResult> predictions) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("  🔮 PRICE PREDICTIONS");
        System.out.println("=".repeat(70));

        if (predictions.isEmpty()) {
            System.out.println("\n  No predictions available.");
        } else {
            System.out.println();
            predictions.forEach(pred -> {
                System.out.printf("  %-15s → %-15s | Price: %6.2f EUR/kg | Confidence: %s %5.1f%%%n",
                        pred.productType().getDisplayName(),
                        pred.destination().getName(),
                        pred.predictedPrice(),
                        pred.confidenceLevel().getIndicator(),
                        pred.confidenceScore() * 100
                );
            });
        }

        System.out.println("=".repeat(70));
    }

    // FIXED LINES 94-98
    public void displayExports(List<ExportData> exports, int limit) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("  📦 EXPORT DATA");
        System.out.println("=".repeat(70));

        if (exports.isEmpty()) {
            System.out.println("\n  No export data available.");
        } else {
            System.out.println();
            exports.stream()
                    .limit(limit)
                    .forEach(export -> {
                        // USING ALIAS METHODS from ExportData.java
                        System.out.printf("  %s | %-15s → %-15s | %.1f tons @ %.2f EUR/kg%n",
                                export.exportDate(),           // alias for date()
                                export.productType().getDisplayName(),
                                export.destination(),          // alias for destinationCountry()
                                export.quantity(),             // alias for volume()
                                export.pricePerUnit()          // alias for pricePerTon()
                        );
                    });

            if (exports.size() > limit) {
                System.out.printf("%n  ... and %d more records%n", exports.size() - limit);
            }
        }

        System.out.println("=".repeat(70));
    }

    public void displayReport(MarketReport report) {
        if (report == null) {
            System.out.println("\n  No report available.");
            return;
        }

        System.out.println("\n" + "=".repeat(70));
        System.out.println("  📄 MARKET REPORT");
        System.out.println("=".repeat(70));
        System.out.println("\nTitle: " + report.title());
        System.out.println("Type: " + report.reportType().getDisplayName());
        System.out.println("Generated: " + report.generatedAt());
        System.out.println("Predictions: " + report.predictions().size());
        System.out.println("\n" + "─".repeat(70));
        System.out.println("\nContent:");
        System.out.println(report.content());
        System.out.println("\n" + "=".repeat(70));
    }

    public void displayChart(String title, Map<String, Double> data) {
        if (chartStrategy != null) {
            chartStrategy.render(title, data);
        } else {
            System.out.println("No chart strategy selected");
        }
    }

    public void setChartStrategy(ChartStrategy strategy) {
        this.chartStrategy = strategy;
    }

    public void displayProductMenu() {
        System.out.println("\n📦 SELECT PRODUCT:");
        ProductType[] products = ProductType.values();
        for (int i = 0; i < products.length; i++) {
            System.out.printf("  %d. %s%n", i + 1, products[i].getDisplayName());
        }
        System.out.print("\nEnter choice: ");
    }

    public void displayCountryMenu() {
        System.out.println("\n🌍 SELECT COUNTRY:");
        Country[] countries = Country.values();
        for (int i = 0; i < countries.length; i++) {
            System.out.printf("  %d. %s%n", i + 1, countries[i].getName());
        }
        System.out.print("\nEnter choice: ");
    }

    public void displayChartTypeMenu() {
        System.out.println("\n📊 SELECT CHART TYPE:");
        System.out.println("  1. Bar Chart");
        System.out.println("  2. Line Chart");
        System.out.println("  3. Pie Chart");
        System.out.print("\nEnter choice: ");
    }

    public void displaySuccess(String message) {
        System.out.println("\n✅ " + message);
    }

    public void displayError(String message) {
        System.out.println("\n❌ Error: " + message);
    }

    public void displayInfo(String message) {
        System.out.println("\nℹ️  " + message);
    }

    public void pause() {
        System.out.print("\nPress Enter to continue...");
        try {
            System.in.read();
        } catch (Exception e) {
            // Ignore
        }
    }
}