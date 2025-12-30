package tn.agricultureai.service.report;

import tn.agricultureai.domain.model.*;
import java.util.List;

/**
 * Interface for AI-powered report generation.
 * Demonstrates: Service interface for LLM integration.
 *
 * @author Your Name
 */
public interface ReportGenerator {

    /**
     * Generate market report from predictions
     *
     * @param reportType Type of report to generate
     * @param predictions Prediction data
     * @return Generated report
     */
    MarketReport generateReport(
            MarketReport.ReportType reportType,
            List<PredictionResult> predictions
    );

    /**
     * Generate product-focused report
     *
     * @param productType Product to focus on
     * @param predictions Predictions for this product
     * @return Product analysis report
     */
    MarketReport generateProductReport(
            ProductType productType,
            List<PredictionResult> predictions
    );

    /**
     * Generate country-focused report
     *
     * @param country Country to analyze
     * @param predictions Predictions for this country
     * @return Country analysis report
     */
    MarketReport generateCountryReport(
            Country country,
            List<PredictionResult> predictions
    );

    /**
     * Generate custom report with specific prompt
     *
     * @param customPrompt User-provided prompt
     * @param predictions Data context
     * @return Custom report
     */
    MarketReport generateCustomReport(
            String customPrompt,
            List<PredictionResult> predictions
    );

    /**
     * Check if service is available
     *
     * @return true if ready
     */
    boolean isAvailable();

    /**
     * Get LLM model name
     *
     * @return Model identifier
     */
    String getModelName();
}