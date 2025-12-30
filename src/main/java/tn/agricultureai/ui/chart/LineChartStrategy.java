package tn.agricultureai.ui.chart;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Line chart implementation using console characters.
 * Demonstrates: Strategy pattern, ASCII visualization.
 *
 * @author Your Name
 */
public class LineChartStrategy implements ChartStrategy {

    private static final int CHART_HEIGHT = 10;
    private static final int CHART_WIDTH = 60;

    @Override
    public void render(String title, Map<String, Double> data) {
        if (data == null || data.isEmpty()) {
            System.out.println("No data to display");
            return;
        }

        System.out.println("\n" + "=".repeat(70));
        System.out.println("  " + title + " (Line Chart)");
        System.out.println("=".repeat(70));

        // Convert to list for indexing
        List<Map.Entry<String, Double>> entries = new ArrayList<>(data.entrySet());

        // Find min and max values for scaling
        double maxValue = data.values().stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(1.0);

        double minValue = data.values().stream()
                .mapToDouble(Double::doubleValue)
                .min()
                .orElse(0.0);

        // Render chart
        for (int row = CHART_HEIGHT; row >= 0; row--) {
            double threshold = minValue + (maxValue - minValue) * row / CHART_HEIGHT;

            System.out.printf("%8.2f |", threshold);

            for (int col = 0; col < entries.size(); col++) {
                double value = entries.get(col).getValue();

                if (Math.abs(value - threshold) < (maxValue - minValue) / CHART_HEIGHT) {
                    System.out.print(" ● ");
                } else if (value > threshold) {
                    System.out.print(" │ ");
                } else {
                    System.out.print("   ");
                }
            }
            System.out.println();
        }

        // X-axis
        System.out.print("         └");
        System.out.println("─".repeat(entries.size() * 3));

        // Labels
        System.out.print("          ");
        for (int i = 0; i < entries.size(); i++) {
            System.out.printf("%-3d", i + 1);
        }
        System.out.println();

        // Legend
        System.out.println("\nLegend:");
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<String, Double> entry = entries.get(i);
            System.out.printf("  %d: %s (%.2f)%n", i + 1, entry.getKey(), entry.getValue());
        }

        System.out.println("=".repeat(70));
    }

    @Override
    public String getChartType() {
        return "Line Chart";
    }

    @Override
    public boolean supportsDataSize(int dataSize) {
        return dataSize > 0 && dataSize <= 15;
    }
}