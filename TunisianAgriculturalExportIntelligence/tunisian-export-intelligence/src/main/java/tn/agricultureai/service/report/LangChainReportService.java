package tn.agricultureai.service.report;

import lombok.extern.slf4j.Slf4j;
import tn.agricultureai.domain.exception.ReportGenerationException;
import tn.agricultureai.domain.model.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LangChain4j-based report generation service.
 * Demonstrates: LLM integration (simulated for exam).
 *
 * In production, this would use actual LangChain4j with OpenAI/Ollama.
 * For exam purposes, we simulate the report generation.
 *
 * @author Your Name
 */
@Slf4j
public class LangChainReportService implements ReportGenerator {

    private final String modelName;
    private final boolean available;

    // In real implementation:
    // private final ChatLanguageModel chatModel;
    // private final AiServices aiServices;

    public LangChainReportService() {
        this.modelName = "GPT-4-Simulated";
        this.available = true;

        // In real implementation:
        // this.chatModel = OpenAiChatModel.builder()
        //     .apiKey(System.getenv("OPENAI_API_KEY"))
        //     .modelName("gpt-4")
        //     .build();

        log.info("Initialized LangChain report service with model: {}", modelName);
    }

    @Override
    public MarketReport generateReport(
            MarketReport.ReportType reportType,
            List<PredictionResult> predictions
    ) {
        log.info("Generating {} report with {} predictions", reportType, predictions.size());

        if (!available) {
            throw ReportGenerationException.invalidApiKey(modelName);
        }

        try {
            String prompt = buildPrompt(reportType, predictions);
            String content = generateContent(prompt, predictions);
            String title = generateTitle(reportType, predictions);

            return new MarketReport(
                    title,
                    content,
                    reportType,
                    predictions
            );

        } catch (Exception e) {
            log.error("Report generation failed", e);
            throw new ReportGenerationException(
                    "Failed to generate report: " + e.getMessage(),
                    reportType,
                    modelName
            );
        }
    }

    @Override
    public MarketReport generateProductReport(
            ProductType productType,
            List<PredictionResult> predictions
    ) {
        List<PredictionResult> productPredictions = predictions.stream()
                .filter(pred -> pred.productType().equals(productType))
                .collect(Collectors.toList());

        return generateReport(MarketReport.ReportType.PRODUCT_FOCUS, productPredictions);
    }

    @Override
    public MarketReport generateCountryReport(
            Country country,
            List<PredictionResult> predictions
    ) {
        List<PredictionResult> countryPredictions = predictions.stream()
                .filter(pred -> pred.destination().equals(country))
                .collect(Collectors.toList());

        return generateReport(MarketReport.ReportType.COUNTRY_FOCUS, countryPredictions);
    }

    @Override
    public MarketReport generateCustomReport(
            String customPrompt,
            List<PredictionResult> predictions
    ) {
        log.info("Generating custom report with prompt: {}", customPrompt);

        String content = generateContent(customPrompt, predictions);

        return new MarketReport(
                "Custom Market Analysis",
                content,
                MarketReport.ReportType.CUSTOM,
                predictions
        );
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    /**
     * Build prompt for LLM based on report type
     */
    private String buildPrompt(
            MarketReport.ReportType reportType,
            List<PredictionResult> predictions
    ) {
        String dataContext = formatPredictionsForPrompt(predictions);

        return switch (reportType) {
            case DAILY_SUMMARY -> String.format(
                    "Generate a daily market summary for Tunisian agricultural exports. " +
                            "Analyze the following predictions and provide insights:\n\n%s\n\n" +
                            "Focus on: price trends, market opportunities, and key takeaways.",
                    dataContext
            );

            case WEEKLY_ANALYSIS -> String.format(
                    "Create a comprehensive weekly analysis of Tunisian agricultural export markets. " +
                            "Based on these predictions:\n\n%s\n\n" +
                            "Include: trend analysis, regional performance, and strategic recommendations.",
                    dataContext
            );

            case PRODUCT_FOCUS -> String.format(
                    "Analyze the export prospects for the following product predictions:\n\n%s\n\n" +
                            "Provide: market position, competitive advantages, and growth opportunities.",
                    dataContext
            );

            case COUNTRY_FOCUS -> String.format(
                    "Analyze export opportunities to the target market based on:\n\n%s\n\n" +
                            "Cover: market dynamics, pricing strategies, and expansion potential.",
                    dataContext
            );

            case TREND_ANALYSIS -> String.format(
                    "Identify and analyze key trends in Tunisian agricultural exports:\n\n%s\n\n" +
                            "Highlight: emerging patterns, seasonal factors, and future outlook.",
                    dataContext
            );

            case CUSTOM -> "Analyze the provided agricultural export data and generate insights.";
        };
    }

    /**
     * Format predictions as context for LLM prompt
     */
    private String formatPredictionsForPrompt(List<PredictionResult> predictions) {
        if (predictions.isEmpty()) {
            return "No prediction data available.";
        }

        return predictions.stream()
                .limit(10) // Limit to avoid token overflow
                .map(pred -> String.format(
                        "- %s to %s: %.2f EUR/kg (Confidence: %.0f%%)",
                        pred.productType().getDisplayName(),
                        pred.destination().getName(),
                        pred.predictedPrice(),
                        pred.confidenceScore() * 100
                ))
                .collect(Collectors.joining("\n"));
    }

    /**
     * Generate content using LLM
     * In real implementation: chatModel.generate(prompt)
     */
    private String generateContent(String prompt, List<PredictionResult> predictions) {
        // Simulated LLM response based on predictions
        log.debug("Generating content with prompt length: {} chars", prompt.length());

        // In real implementation:
        // return chatModel.generate(prompt);

        // Simulated response
        return generateSimulatedContent(predictions);
    }

    /**
     * Generate title based on report type
     */
    private String generateTitle(
            MarketReport.ReportType reportType,
            List<PredictionResult> predictions
    ) {
        return switch (reportType) {
            case DAILY_SUMMARY -> "Daily Market Summary - " + LocalDateTime.now().toLocalDate();
            case WEEKLY_ANALYSIS -> "Weekly Market Analysis";
            case PRODUCT_FOCUS -> {
                if (!predictions.isEmpty()) {
                    yield "Product Analysis: " + predictions.get(0).productType().getDisplayName();
                }
                yield "Product Market Analysis";
            }
            case COUNTRY_FOCUS -> {
                if (!predictions.isEmpty()) {
                    yield "Market Analysis: " + predictions.get(0).destination().getName();
                }
                yield "Country Market Analysis";
            }
            case TREND_ANALYSIS -> "Market Trend Analysis Q" +
                    ((LocalDateTime.now().getMonthValue() - 1) / 3 + 1);
            case CUSTOM -> "Custom Market Report";
        };
    }

    /**
     * Generate simulated AI content (for exam demonstration)
     */
    private String generateSimulatedContent(List<PredictionResult> predictions) {
        if (predictions.isEmpty()) {
            return "No prediction data available for analysis.";
        }

        double avgPrice = predictions.stream()
                .mapToDouble(PredictionResult::predictedPrice)
                .average()
                .orElse(0.0);

        double avgConfidence = predictions.stream()
                .mapToDouble(PredictionResult::confidenceScore)
                .average()
                .orElse(0.0);

        long euDestinations = predictions.stream()
                .filter(pred -> pred.destination().getRegion() == Country.Region.EU)
                .count();

        return String.format("""
            Market Analysis Summary
            
            Our analysis of %d export predictions reveals promising market conditions for \
            Tunisian agricultural products. The average predicted price stands at %.2f EUR/kg, \
            with an overall confidence level of %.0f%%.
            
            Key Findings:
            
            Regional Distribution: %d of the analyzed exports target EU markets, which continue \
            to offer premium prices for quality Tunisian products. The EU's growing demand for \
            sustainable and traceable agricultural goods presents significant opportunities for \
            Tunisian exporters.
            
            Price Outlook: Current predictions suggest stable to slightly increasing prices across \
            most product categories. This trend reflects both strong international demand and the \
            competitive positioning of Tunisian agricultural exports.
            
            Market Confidence: With an average confidence score of %.0f%%, our predictions indicate \
            a relatively stable market environment. However, exporters should remain vigilant to \
            seasonal variations and global market dynamics.
            
            Strategic Recommendations:
            - Focus on high-confidence predictions for immediate export planning
            - Diversify destination markets to mitigate regional risks
            - Invest in quality improvements to capture premium market segments
            - Monitor EU regulatory changes that may impact export requirements
            
            The outlook for Tunisian agricultural exports remains positive, with sustained demand \
            from traditional markets and emerging opportunities in new regions.
            """,
                predictions.size(),
                avgPrice,
                avgConfidence * 100,
                euDestinations,
                avgConfidence * 100
        );
    }
}