package tn.agricultureai.ui.observer;

import lombok.Getter;
import tn.agricultureai.domain.model.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Observer that tracks and aggregates statistics from dashboard events.
 * Demonstrates: Observer pattern, statistics tracking, thread-safe operations.
 *
 * @author Your Name
 */
@Getter
public class StatisticsObserver implements DashboardObserver {

    // Thread-safe statistics storage
    private final Map<String, Integer> eventCounts = new ConcurrentHashMap<>();
    private final List<UpdateEvent> eventHistory = Collections.synchronizedList(new ArrayList<>());
    private final Map<LocalDateTime, Integer> eventTimeline = new ConcurrentHashMap<>();

    private int totalDataUpdates = 0;
    private int totalPredictions = 0;
    private int totalReports = 0;

    private LocalDateTime firstEvent;
    private LocalDateTime lastEvent;

    /**
     * Track data update events
     */
    @Override
    public void onDataUpdated(UpdateEvent event) {
        recordEvent(event, "DATA_UPDATE");
        totalDataUpdates++;
        logEvent("📊 Data Update", event);
    }

    /**
     * Track prediction completion events
     */
    @Override
    public void onPredictionCompleted(UpdateEvent event) {
        recordEvent(event, "PREDICTION");
        totalPredictions++;
        logEvent("🔮 Prediction", event);

        // If event contains prediction result, track it
        if (event.data() instanceof PredictionResult prediction) {
            trackPrediction(prediction);
        }
    }

    /**
     * Track report generation events
     */
    @Override
    public void onReportGenerated(UpdateEvent event) {
        recordEvent(event, "REPORT");
        totalReports++;
        logEvent("📄 Report", event);

        // If event contains report, track it
        if (event.data() instanceof MarketReport report) {
            trackReport(report);
        }
    }

    /**
     * Record event in history
     */
    private void recordEvent(UpdateEvent event, String category) {
        // Update timestamps
        if (firstEvent == null) {
            firstEvent = LocalDateTime.now();
        }
        lastEvent = LocalDateTime.now();

        // Store in history
        eventHistory.add(event);

        // Update counters
        eventCounts.merge(category, 1, Integer::sum);

        // Timeline tracking (hourly)
        LocalDateTime hour = lastEvent.withMinute(0).withSecond(0).withNano(0);
        eventTimeline.merge(hour, 1, Integer::sum);
    }

    /**
     * Log event to console
     */
    private void logEvent(String prefix, UpdateEvent event) {
        System.out.printf("[STATS] %s: %s%n", prefix, event.message());
    }

    /**
     * Track prediction-specific statistics
     */
    private final Map<ProductType, Integer> predictionsByProduct = new ConcurrentHashMap<>();
    private final Map<Country, Integer> predictionsByCountry = new ConcurrentHashMap<>();
    private final List<Double> confidenceScores = Collections.synchronizedList(new ArrayList<>());

    private void trackPrediction(PredictionResult prediction) {
        predictionsByProduct.merge(prediction.productType(), 1, Integer::sum);
        predictionsByCountry.merge(prediction.destination(), 1, Integer::sum);
        confidenceScores.add(prediction.confidenceScore());
    }

    /**
     * Track report-specific statistics
     */
    private final Map<MarketReport.ReportType, Integer> reportsByType = new ConcurrentHashMap<>();
    private final List<Integer> reportWordCounts = Collections.synchronizedList(new ArrayList<>());

    private void trackReport(MarketReport report) {
        reportsByType.merge(report.reportType(), 1, Integer::sum);
        if (report.metadata() != null) {
            reportWordCounts.add(report.metadata().wordCount());
        }
    }

    /**
     * Get summary statistics
     */
    public StatisticsSummary getSummary() {
        long sessionDuration = 0;
        if (firstEvent != null && lastEvent != null) {
            sessionDuration = java.time.Duration.between(firstEvent, lastEvent).toSeconds();
        }

        double avgConfidence = confidenceScores.isEmpty() ? 0.0 :
                confidenceScores.stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0.0);

        double avgReportLength = reportWordCounts.isEmpty() ? 0.0 :
                reportWordCounts.stream()
                        .mapToInt(Integer::intValue)
                        .average()
                        .orElse(0.0);

        return new StatisticsSummary(
                totalDataUpdates,
                totalPredictions,
                totalReports,
                eventHistory.size(),
                sessionDuration,
                avgConfidence,
                avgReportLength,
                predictionsByProduct,
                predictionsByCountry,
                reportsByType
        );
    }

    /**
     * Get event timeline for visualization
     */
    public Map<LocalDateTime, Integer> getEventTimeline() {
        return new TreeMap<>(eventTimeline);
    }

    /**
     * Get recent events (last N)
     */
    public List<UpdateEvent> getRecentEvents(int count) {
        int size = eventHistory.size();
        int start = Math.max(0, size - count);
        return new ArrayList<>(eventHistory.subList(start, size));
    }

    /**
     * Get events by type
     */
    public List<UpdateEvent> getEventsByType(UpdateEvent.EventType type) {
        return eventHistory.stream()
                .filter(event -> event.type() == type)
                .collect(Collectors.toList());
    }

    /**
     * Get most active hour
     */
    public Optional<LocalDateTime> getMostActiveHour() {
        return eventTimeline.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    /**
     * Get product prediction distribution
     */
    public Map<ProductType, Double> getProductPredictionDistribution() {
        int total = predictionsByProduct.values().stream()
                .mapToInt(Integer::intValue)
                .sum();

        if (total == 0) {
            return Map.of();
        }

        return predictionsByProduct.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (entry.getValue() * 100.0) / total
                ));
    }

    /**
     * Get country prediction distribution
     */
    public Map<Country, Double> getCountryPredictionDistribution() {
        int total = predictionsByCountry.values().stream()
                .mapToInt(Integer::intValue)
                .sum();

        if (total == 0) {
            return Map.of();
        }

        return predictionsByCountry.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (entry.getValue() * 100.0) / total
                ));
    }

    /**
     * Print statistics report
     */
    public void printReport() {
        StatisticsSummary summary = getSummary();

        System.out.println("\n" + "=".repeat(70));
        System.out.println("  📊 STATISTICS OBSERVER REPORT");
        System.out.println("=".repeat(70));
        System.out.println();

        // Session info
        System.out.println("Session Information:");
        System.out.printf("  First Event: %s%n",
                firstEvent != null ? firstEvent : "N/A");
        System.out.printf("  Last Event: %s%n",
                lastEvent != null ? lastEvent : "N/A");
        System.out.printf("  Duration: %d seconds%n", summary.sessionDuration());
        System.out.println();

        // Event counts
        System.out.println("Event Counts:");
        System.out.printf("  Data Updates: %d%n", summary.totalDataUpdates());
        System.out.printf("  Predictions: %d%n", summary.totalPredictions());
        System.out.printf("  Reports: %d%n", summary.totalReports());
        System.out.printf("  Total Events: %d%n", summary.totalEvents());
        System.out.println();

        // Predictions
        if (!predictionsByProduct.isEmpty()) {
            System.out.println("Predictions by Product:");
            predictionsByProduct.forEach((product, count) ->
                    System.out.printf("  %s: %d%n", product.getDisplayName(), count));
            System.out.printf("  Average Confidence: %.1f%%%n",
                    summary.avgPredictionConfidence() * 100);
            System.out.println();
        }

        // Reports
        if (!reportsByType.isEmpty()) {
            System.out.println("Reports by Type:");
            reportsByType.forEach((type, count) ->
                    System.out.printf("  %s: %d%n", type.getDisplayName(), count));
            System.out.printf("  Average Length: %.0f words%n",
                    summary.avgReportWordCount());
            System.out.println();
        }

        System.out.println("=".repeat(70));
    }

    /**
     * Reset all statistics
     */
    public void reset() {
        eventCounts.clear();
        eventHistory.clear();
        eventTimeline.clear();
        predictionsByProduct.clear();
        predictionsByCountry.clear();
        reportsByType.clear();
        confidenceScores.clear();
        reportWordCounts.clear();

        totalDataUpdates = 0;
        totalPredictions = 0;
        totalReports = 0;
        firstEvent = null;
        lastEvent = null;

        System.out.println("📊 Statistics observer reset");
    }

    /**
     * Record for statistics summary
     */
    public record StatisticsSummary(
            int totalDataUpdates,
            int totalPredictions,
            int totalReports,
            int totalEvents,
            long sessionDuration,
            double avgPredictionConfidence,
            double avgReportWordCount,
            Map<ProductType, Integer> predictionsByProduct,
            Map<Country, Integer> predictionsByCountry,
            Map<MarketReport.ReportType, Integer> reportsByType
    ) {
        public double getEventsPerMinute() {
            if (sessionDuration == 0) return 0.0;
            return (totalEvents * 60.0) / sessionDuration;
        }

        @Override
        public String toString() {
            return String.format(
                    "Statistics Summary: %d events over %d seconds (%.2f events/min)",
                    totalEvents, sessionDuration, getEventsPerMinute()
            );
        }
    }
}