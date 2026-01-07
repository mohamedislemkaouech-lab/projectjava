package tn.agricultureai.repository;

import tn.agricultureai.domain.model.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class PredictionRepository extends InMemoryRepository<PredictionResult, String> {

    private long idCounter = 0;

    @Override
    protected String extractId(PredictionResult entity) {
        return "PRED-" + (++idCounter);
    }

    @Override
    public PredictionResult save(PredictionResult entity) {
        String id = "PRED-" + (++idCounter);
        storage.put(id, entity);
        return entity;
    }

    public List<PredictionResult> findByProductType(ProductType productType) {
        return findBy(pred -> pred.productType().equals(productType));
    }

    public List<PredictionResult> findByDestination(Country destination) {
        return findBy(pred -> pred.destination().equals(destination));
    }

    public List<PredictionResult> findByConfidenceLevel(ConfidenceLevel level) {
        return findBy(pred -> pred.confidenceLevel().equals(level));
    }

    public List<PredictionResult> findReliablePredictions() {
        return findBy(PredictionResult::isReliable);
    }

    public List<PredictionResult> findByTimeRange(
            LocalDateTime fromTime,
            LocalDateTime toTime
    ) {
        return findBy(pred -> {
            LocalDateTime predTime = pred.predictionTime();
            return !predTime.isBefore(fromTime) && !predTime.isAfter(toTime);
        });
    }

    public List<PredictionResult> findRecentPredictions(int hours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        return findBy(pred -> pred.predictionTime().isAfter(cutoff));
    }

    public List<PredictionResult> findByModel(String modelName) {
        return findBy(pred -> pred.modelName().equals(modelName));
    }

    public Map<ProductType, Double> getAverageConfidenceByProduct() {
        return storage.values().stream()
                .collect(Collectors.groupingBy(
                        PredictionResult::productType,
                        Collectors.averagingDouble(PredictionResult::confidenceScore)
                ));
    }

    public Map<Country, Double> getAveragePriceByDestination() {
        return storage.values().stream()
                .collect(Collectors.groupingBy(
                        PredictionResult::destination,
                        Collectors.averagingDouble(PredictionResult::predictedPrice)
                ));
    }

    public Map<ConfidenceLevel, Long> getConfidenceLevelDistribution() {
        return storage.values().stream()
                .collect(Collectors.groupingBy(
                        PredictionResult::confidenceLevel,
                        Collectors.counting()
                ));
    }

    public List<PredictionResult> findTopConfidentPredictions(int n) {
        return storage.values().stream()
                .sorted(Comparator.comparingDouble(PredictionResult::confidenceScore).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    public Map<ProductType, Map<Country, List<PredictionResult>>>
    groupByProductAndDestination() {
        return storage.values().stream()
                .collect(Collectors.groupingBy(
                        PredictionResult::productType,
                        Collectors.groupingBy(PredictionResult::destination)
                ));
    }

    // FIXED LINE 160
    public double calculateAverageError(List<ExportData> actualData) {
        if (actualData.isEmpty()) return 0.0;

        return actualData.stream()
                .mapToDouble(actual -> {
                    Optional<PredictionResult> prediction = findFirst(pred ->
                            pred.productType().equals(actual.productType()) &&
                                    pred.destination().getName().equals(actual.destinationCountry())  // FIXED
                    );

                    return prediction.map(pred ->
                            Math.abs(pred.predictedPrice() - actual.pricePerTon())  // FIXED: pricePerUnit to pricePerTon
                    ).orElse(0.0);
                })
                .average()
                .orElse(0.0);
    }

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