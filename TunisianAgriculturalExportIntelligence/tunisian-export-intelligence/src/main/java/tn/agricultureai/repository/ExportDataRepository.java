package tn.agricultureai.repository;

import tn.agricultureai.domain.model.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository for ExportData entities.
 * Demonstrates: Concrete repository, custom query methods, Stream API.
 *
 * @author Your Name
 */
public class ExportDataRepository extends InMemoryRepository<ExportData, String> {

    @Override
    protected String extractId(ExportData entity) {
        return entity.id();
    }

    /**
     * Find exports by product type
     * Demonstrates: Stream filter operation
     */
    public List<ExportData> findByProductType(ProductType productType) {
        return findBy(export -> export.productType().equals(productType));
    }

    /**
     * Find exports by destination country
     */
    public List<ExportData> findByDestination(Country destination) {
        return findBy(export -> export.destination().equals(destination));
    }

    /**
     * Find exports by product and destination
     * Demonstrates: Complex predicate
     */
    public List<ExportData> findByProductAndDestination(
            ProductType productType,
            Country destination
    ) {
        return findBy(export ->
                export.productType().equals(productType) &&
                        export.destination().equals(destination)
        );
    }

    /**
     * Find exports within date range
     * Demonstrates: Date comparison in streams
     */
    public List<ExportData> findByDateRange(LocalDate fromDate, LocalDate toDate) {
        return findBy(export -> {
            LocalDate exportDate = export.exportDate();
            return !exportDate.isBefore(fromDate) && !exportDate.isAfter(toDate);
        });
    }

    /**
     * Find recent exports (last N days)
     */
    public List<ExportData> findRecent(int days) {
        LocalDate cutoffDate = LocalDate.now().minusDays(days);
        return findBy(export -> export.exportDate().isAfter(cutoffDate));
    }

    /**
     * Find exports to EU countries only
     * Demonstrates: Nested property access in predicate
     */
    public List<ExportData> findEuExports() {
        return findBy(ExportData::isEuExport);
    }

    /**
     * Calculate total export value by product type
     * Demonstrates: Stream grouping and reducing
     */
    public Map<ProductType, Double> getTotalValueByProduct() {
        return storage.values().stream()
                .collect(Collectors.groupingBy(
                        ExportData::productType,
                        Collectors.summingDouble(ExportData::getTotalValue)
                ));
    }

    /**
     * Calculate total export value by destination
     */
    public Map<Country, Double> getTotalValueByDestination() {
        return storage.values().stream()
                .collect(Collectors.groupingBy(
                        ExportData::destination,
                        Collectors.summingDouble(ExportData::getTotalValue)
                ));
    }

    /**
     * Get average price per unit by product
     * Demonstrates: Stream averaging
     */
    public Map<ProductType, Double> getAveragePriceByProduct() {
        return storage.values().stream()
                .collect(Collectors.groupingBy(
                        ExportData::productType,
                        Collectors.averagingDouble(ExportData::pricePerUnit)
                ));
    }

    /**
     * Find top N exports by value
     * Demonstrates: Sorting and limiting
     */
    public List<ExportData> findTopExportsByValue(int n) {
        return storage.values().stream()
                .sorted(Comparator.comparingDouble(ExportData::getTotalValue).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    /**
     * Group exports by month
     * Demonstrates: Complex grouping
     */
    public Map<String, List<ExportData>> groupByMonth() {
        return storage.values().stream()
                .collect(Collectors.groupingBy(export ->
                        export.exportDate().getYear() + "-" +
                                String.format("%02d", export.exportDate().getMonthValue())
                ));
    }

    /**
     * Get export statistics
     * Demonstrates: Stream statistics
     */
    public ExportStatistics calculateStatistics() {
        DoubleSummaryStatistics priceStats = storage.values().stream()
                .mapToDouble(ExportData::pricePerUnit)
                .summaryStatistics();

        DoubleSummaryStatistics quantityStats = storage.values().stream()
                .mapToDouble(ExportData::quantity)
                .summaryStatistics();

        double totalValue = storage.values().stream()
                .mapToDouble(ExportData::getTotalValue)
                .sum();

        return new ExportStatistics(
                storage.size(),
                priceStats.getAverage(),
                priceStats.getMin(),
                priceStats.getMax(),
                quantityStats.getSum(),
                totalValue
        );
    }

    /**
     * Record for export statistics
     */
    public record ExportStatistics(
            long totalRecords,
            double averagePrice,
            double minPrice,
            double maxPrice,
            double totalQuantity,
            double totalValue
    ) {
        @Override
        public String toString() {
            return String.format("""
                Export Statistics:
                  Total Records: %d
                  Average Price: %.2f EUR/kg
                  Price Range: %.2f - %.2f EUR/kg
                  Total Quantity: %.2f tons
                  Total Value: %.2f EUR
                """,
                    totalRecords,
                    averagePrice,
                    minPrice,
                    maxPrice,
                    totalQuantity,
                    totalValue
            );
        }
    }
}