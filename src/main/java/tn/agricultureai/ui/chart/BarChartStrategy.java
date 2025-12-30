package tn.agricultureai.ui.chart;

import java.util.Map;

/**
 * Bar chart implementation using console characters.
 * Demonstrates: Strategy pattern implementation, ASCII art rendering.
 *
 * @author Your Name
 */
public class BarChartStrategy implements ChartStrategy {

    private static final int MAX_BAR_WIDTH = 50;
    private static final String BAR_CHAR = "█";

    @Override
    public void render(String title, Map<String, Double> data) {
        if (data == null || data.isEmpty()) {
            System.out.println("No data to display");
            return;
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("  " + title + " (Bar Chart)");
        System.out.println("=".repeat(60));

        // Find max value for scaling
        double maxValue = data.values().stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(1.0);

        // Render each bar
        data.forEach((label, value) -> {
            int barLength = (int) ((value / maxValue) * MAX_BAR_WIDTH);
            String bar = BAR_CHAR.repeat(Math.max(0, barLength));

            System.out.printf("%-20s | %-50s %.2f%n",
                    truncate(label, 20),
                    bar,
                    value
            );
        });

        System.out.println("=".repeat(60));
    }

    @Override
    public String getChartType() {
        return "Bar Chart";
    }

    @Override
    public boolean supportsDataSize(int dataSize) {
        return dataSize > 0 && dataSize <= 20; // Reasonable limit for console
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