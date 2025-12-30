package tn.agricultureai.repository;

import tn.agricultureai.domain.model.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository for PredictionResult entities.
 * Demonstrates: Custom queries, Stream API operations, statistics.
 *
 * @author Your Name
 */
public class PredictionRepository extends InMemoryRepository<PredictionResult, String> {

    // Custom ID generation for predictions
    private long idCounter = 0;

    @Override
    protected String extractId(PredictionResult entity) {
        // Generate ID if needed (for demo purposes)
        return "PRED-" + (++idCounter);
    }

    @Override
    public PredictionResult save(PredictionResult entity) {
        // Override to handle ID generation
        String id = "PRED-" + (++idCounter);
        storage.put(id, entity);
        return entity;
    }

    /**
     * Find predictions by product type
     */
    public List<PredictionResult> findByProductType(ProductType productType) {
        return findBy(pred -> pred.productType().equals(productType));
    }

    /**
     * Find predictions by destination
     */
    public List<PredictionResult> findByDestination(Country destination) {
        return findBy(pred -> pred.destination().equals(destination));
    }

    /**
     * Find predictions by confidence level
     */
    public List<PredictionResult> findByConfidenceLevel(ConfidenceLevel level) {
        return findBy(pred -> pred.confidenceLevel().equals(level));
    }

    /**
     * Find reliable predictions only (confidence >= MEDIUM)
     * Demonstrates: Method reference
     */
    public List<PredictionResult> findReliablePredictions() {
        return findBy(PredictionResult::isReliable);
    }

    /**
     * Find predictions within time range
     */
    public List<PredictionResult> findByTimeRange(
            LocalDateTime fromTime,
            LocalDateTime toTime
    ) {
        return findBy(pred -> {
            LocalDateTime predTime = pred.predictionTime();
            return !predTime.isBefore(fromTime) && !predTime.isAfter(toTime);
        });
    }

    /**
     * Find recent predictions (last N hours)
     */
    public List<PredictionResult> findRecentPredictions(int hours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        return findBy(pred -> pred.predictionTime().isAfter(cutoff));
    }

    /**
     * Find predictions by model name
     */
    public List<PredictionResult> findByModel(String modelName) {
        return findBy(pred -> pred.modelName().equals(modelName));
    }

    /**
     * Get average confidence by product type
     * Demonstrates: Grouping and averaging
     */
    public Map<ProductType, Double> getAverageConfidenceByProduct() {
        return storage.values().stream()
                .collect(Collectors.groupingBy(
                        PredictionResult::productType,
                        Collectors.averagingDouble(PredictionResult::confidenceScore)
                ));
    }

    /**
     * Get average predicted price by destination
     */
    public Map<Country, Double> getAveragePriceByDestination() {
        return storage.values().stream()
                .collect(Collectors.groupingBy(
                        PredictionResult::destination,
                        Collectors.averagingDouble(PredictionResult::predictedPrice)
                ));
    }

    /**
     * Get confidence level distribution
     * Demonstrates: Grouping and counting
     */
    public Map<ConfidenceLevel, Long> getConfidenceLevelDistribution() {
        return storage.values().stream()
                .collect(Collectors.groupingBy(
                        PredictionResult::confidenceLevel,
                        Collectors.counting()
                ));
    }

    /**
     * Find predictions with highest confidence
     */
    public List<PredictionResult> findTopConfidentPredictions(int n) {
        return storage.values().stream()
                .sorted(Comparator.comparingDouble(PredictionResult::confidenceScore).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    /**
     * Get predictions grouped by product and destination
     * Demonstrates: Multi-level grouping
     */
    public Map<ProductType, Map<Country, List<PredictionResult>>>
    groupByProductAndDestination() {
        return storage.values().stream()
                .collect(Collectors.groupingBy(
                        PredictionResult::productType,
                        Collectors.groupingBy(PredictionResult::destination)
                ));
    }

    /**
     * Calculate prediction accuracy compared to actual data
     * Demonstrates: Stream reduction
     */
    public double calculateAverageError(List<ExportData> actualData) {
        if (actualData.isEmpty()) return 0.0;

        return actualData.stream()
                .mapToDouble(actual -> {
                    // Find matching prediction
                    Optional<PredictionResult> prediction = findFirst(pred ->
                            pred.productType().equals(actual.productType()) &&
                                    pred.destination().equals(actual.destination())
                    );

                    return prediction.map(pred ->
                            Math.abs(pred.predictedPrice() - actual.pricePerUnit())
                    ).orElse(0.0);
                })
                .average()
                .orElse(0.0);
    }

    /**
     * Get prediction statistics
     */
    public PredictionStatistics calculateStatistics() {
        DoubleSummaryStatistics priceStats = storage.values().stream()
                .mapToDouble(PredictionResult::predictedPrice)
                .summaryStatistics();

        DoubleSummaryStatistics confidenceStats = storage.values().stream()
                .mapToDouble(PredictionResult::confidenceScore)
                .summaryStatistics();

        long reliableCount = storage.values().stream()
                .filter(PredictionResult::isReliable)
                .count();

        return new PredictionStatistics(
                storage.size(),
                priceStats.getAverage(),
                priceStats.getMin(),
                priceStats.getMax(),
                confidenceStats.getAverage(),
                reliableCount,
                (double) reliableCount / storage.size() * 100
        );
    }

    /**
     * Record for prediction statistics
     */
    public record PredictionStatistics(
            long totalPredictions,
            double averagePrice,
            double minPrice,
            double maxPrice,
            double averageConfidence,
            long reliableCount,
            double reliablePercentage
    ) {
        @Override
        public String toString() {
            return String.format("""
                Prediction Statistics:
                  Total Predictions: %d
                  Average Price: %.2f EUR/kg
                  Price Range: %.2f - %.2f EUR/kg
                  Average Confidence: %.2f
                  Reliable Predictions: %d (%.1f%%)
                """,
                    totalPredictions,
                    averagePrice,
                    minPrice,
                    maxPrice,
                    averageConfidence,
                    reliableCount,
                    reliablePercentage
            );
        }
    }
}