package tn.agricultureai.ui.chart;

import java.util.Map;

/**
 * Strategy interface for different chart types.
 * Demonstrates: Strategy pattern for algorithm selection.
 *
 * @author Your Name
 */
public interface ChartStrategy {

    /**
     * Render chart with given data
     *
     * @param title Chart title
     * @param data Data to display (label -> value)
     */
    void render(String title, Map<String, Double> data);

    /**
     * Get chart type name
     *
     * @return Chart type (e.g., "Bar Chart", "Line Chart")
     */
    String getChartType();

    /**
     * Check if this chart supports the given data size
     *
     * @param dataSize Number of data points
     * @return true if supported
     */
    boolean supportsDataSize(int dataSize);
}