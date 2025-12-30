package tn.agricultureai.ui.chart;

import java.util.Map;

/**
 * Pie chart implementation using text representation.
 * Demonstrates: Strategy pattern, percentage calculations.
 *
 * @author Your Name
 */
public class PieChartStrategy implements ChartStrategy {

    private static final int BAR_WIDTH = 40;

    @Override
    public void render(String title, Map<String, Double> data) {
        if (data == null || data.isEmpty()) {
            System.out.println("No data to display");
            return;
        }

        System.out.println("\n" + "=".repeat(70));
        System.out.println("  " + title + " (Pie Chart)");
        System.out.println("=".repeat(70));

        // Calculate total
        double total = data.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        if (total == 0) {
            System.out.println("Total is zero, cannot render pie chart");
            return;
        }

        // Render as percentages with visual bars
        data.forEach((label, value) -> {
            double percentage = (value / total) * 100;
            int barLength = (int) ((percentage / 100) * BAR_WIDTH);

            String bar = "█".repeat(Math.max(0, barLength));
            String empty = "░".repeat(Math.max(0, BAR_WIDTH - barLength));

            System.out.printf("%-20s | %s%s | %5.1f%% (%.2f)%n",
                    truncate(label, 20),
                    bar,
                    empty,
                    percentage,
                    value
            );
        });

        System.out.println("─".repeat(70));
        System.out.printf("%-20s | %-40s | Total: %.2f%n", "TOTAL", "", total);
        System.out.println("=".repeat(70));
    }

    @Override
    public String getChartType() {
        return "Pie Chart";
    }

    @Override
    public boolean supportsDataSize(int dataSize) {
        return dataSize > 0 && dataSize <= 10;
    }

    /**
     * Truncate string to max length
     */
    private String truncate(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}