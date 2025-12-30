package tn.agricultureai.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Record representing an AI-generated market intelligence report.
 * Demonstrates: Record with collections, complex nested data.
 */
public record MarketReport(
        String id,
        String title,
        String content,              // LLM-generated text
        ReportType reportType,
        List<PredictionResult> predictions,
        LocalDateTime generatedAt,
        String generatedBy,          // LLM model name
        ReportMetadata metadata
) {
    /**
     * Nested record for report metadata
     * Demonstrates: Records within records
     */
    public record ReportMetadata(
            int totalPredictions,
            double averageConfidence,
            List<String> keywords,
            int wordCount
    ) {
        public ReportMetadata {
            Objects.requireNonNull(keywords, "Keywords cannot be null");
            if (totalPredictions < 0) {
                throw new IllegalArgumentException("Total predictions cannot be negative");
            }
        }
    }

    /**
     * Enum for report types
     */
    public enum ReportType {
        DAILY_SUMMARY("Daily Market Summary"),
        WEEKLY_ANALYSIS("Weekly Analysis"),
        PRODUCT_FOCUS("Product Focus Report"),
        COUNTRY_FOCUS("Country Focus Report"),
        TREND_ANALYSIS("Trend Analysis"),
        CUSTOM("Custom Report");

        private final String displayName;

        ReportType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Compact constructor with validation
     */
    public MarketReport {
        Objects.requireNonNull(title, "Title cannot be null");
        Objects.requireNonNull(content, "Content cannot be null");
        Objects.requireNonNull(reportType, "Report type cannot be null");
        Objects.requireNonNull(predictions, "Predictions list cannot be null");

        // Auto-generate ID if not provided
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }

        // Default generation time
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }

        // Default model name
        if (generatedBy == null || generatedBy.isBlank()) {
            generatedBy = "LangChain4j";
        }

        // Make predictions list immutable
        predictions = List.copyOf(predictions);

        // Auto-generate metadata if not provided
        if (metadata == null) {
            metadata = generateMetadata(predictions, content);
        }
    }

    /**
     * Simplified constructor
     */
    public MarketReport(
            String title,
            String content,
            ReportType reportType,
            List<PredictionResult> predictions
    ) {
        this(null, title, content, reportType, predictions, null, null, null);
    }

    /**
     * Helper method to generate metadata
     */
    private static ReportMetadata generateMetadata(
            List<PredictionResult> predictions,
            String content
    ) {
        int totalPredictions = predictions.size();

        double avgConfidence = predictions.isEmpty() ? 0.0 :
                predictions.stream()
                        .mapToDouble(PredictionResult::confidenceScore)
                        .average()
                        .orElse(0.0);

        List<String> keywords = List.of("export", "price", "prediction", "Tunisia");

        int wordCount = content.split("\\s+").length;

        return new ReportMetadata(totalPredictions, avgConfidence, keywords, wordCount);
    }

    /**
     * Get high-confidence predictions only
     */
    public List<PredictionResult> getHighConfidencePredictions() {
        return predictions.stream()
                .filter(PredictionResult::isReliable)
                .toList();
    }

    /**
     * Get report summary
     */
    public String getSummary() {
        return String.format(
                "%s - %s (%d predictions, avg confidence: %.2f)",
                reportType.getDisplayName(),
                title,
                metadata.totalPredictions(),
                metadata.averageConfidence()
        );
    }

    /**
     * Export as plain text
     */
    public String toPlainText() {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(50)).append("\n");
        sb.append(title).append("\n");
        sb.append("=".repeat(50)).append("\n");
        sb.append("Type: ").append(reportType.getDisplayName()).append("\n");
        sb.append("Generated: ").append(generatedAt).append("\n");
        sb.append("Model: ").append(generatedBy).append("\n");
        sb.append("\n").append(content).append("\n");
        sb.append("\n--- Predictions ---\n");
        predictions.forEach(p -> sb.append("- ").append(p.getSummary()).append("\n"));
        sb.append("\n--- Metadata ---\n");
        sb.append("Total Predictions: ").append(metadata.totalPredictions()).append("\n");
        sb.append("Average Confidence: ").append(String.format("%.2f", metadata.averageConfidence())).append("\n");
        sb.append("Word Count: ").append(metadata.wordCount()).append("\n");
        return sb.toString();
    }
}