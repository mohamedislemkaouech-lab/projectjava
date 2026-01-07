package tn.agricultureai.repository;

import tn.agricultureai.domain.model.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ExportDataRepository extends InMemoryRepository<ExportData, String> {

    @Override
    protected String extractId(ExportData entity) {
        // Generate ID based on content since ExportData doesn't have id()
        return String.format("EXP-%d-%s-%s",
                entity.date().hashCode(),
                entity.productType().name(),
                entity.destinationCountry()
        );
    }

    public List<ExportData> findByProductType(ProductType productType) {
        return findBy(export -> export.productType().equals(productType));
    }

    // FIXED: Use destinationCountry() not destination()
    public List<ExportData> findByDestination(Country destination) {
        return findBy(export -> export.destinationCountry().equals(destination.getName()));
    }

    public List<ExportData> findByProductAndDestination(
            ProductType productType,
            Country destination
    ) {
        return findBy(export ->
                export.productType().equals(productType) &&
                        export.destinationCountry().equals(destination.getName())
        );
    }

    public List<ExportData> findByDateRange(LocalDate fromDate, LocalDate toDate) {
        return findBy(export -> {
            LocalDate exportDate = export.date();  // NOT exportDate()
            return !exportDate.isBefore(fromDate) && !exportDate.isAfter(toDate);
        });
    }

    public List<ExportData> findRecent(int days) {
        LocalDate cutoffDate = LocalDate.now().minusDays(days);
        return findBy(export -> export.date().isAfter(cutoffDate));
    }

    // FIXED: Remove isEuExport() or add it to ExportData
    public List<ExportData> findEuExports() {
        return findBy(export -> {
            String country = export.destinationCountry();
            return country.equals("France") || country.equals("Germany") ||
                    country.equals("Italy") || country.equals("Spain") ||
                    country.equals("United Kingdom");
        });
    }

    public Map<ProductType, Double> getTotalValueByProduct() {
        return storage.values().stream()
                .collect(Collectors.groupingBy(
                        ExportData::productType,
                        Collectors.summingDouble(ExportData::getTotalValue)
                ));
    }

    // FIXED LINE 94: Map Country name, not Country enum
    public Map<String, Double> getTotalValueByDestination() {
        return storage.values().stream()
                .collect(Collectors.groupingBy(
                        ExportData::destinationCountry,
                        Collectors.summingDouble(ExportData::getTotalValue)
                ));
    }

    public Map<ProductType, Double> getAveragePriceByProduct() {
        return storage.values().stream()
                .collect(Collectors.groupingBy(
                        ExportData::productType,
                        Collectors.averagingDouble(ExportData::pricePerTon)  // pricePerTon not pricePerUnit
                ));
    }

    public List<ExportData> findTopExportsByValue(int n) {
        return storage.values().stream()
                .sorted(Comparator.comparingDouble(ExportData::getTotalValue).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    public Map<String, List<ExportData>> groupByMonth() {
        return storage.values().stream()
                .collect(Collectors.groupingBy(export ->
                        export.date().getYear() + "-" +
                                String.format("%02d", export.date().getMonthValue())
                ));
    }

    public ExportStatistics calculateStatistics() {
        DoubleSummaryStatistics priceStats = storage.values().stream()
                .mapToDouble(ExportData::pricePerTon)  // pricePerTon not pricePerUnit
                .summaryStatistics();

        DoubleSummaryStatistics quantityStats = storage.values().stream()
                .mapToDouble(ExportData::volume)  // volume not quantity
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
                  Average Price: %.2f TND/kg
                  Price Range: %.2f - %.2f TND/kg
                  Total Quantity: %.2f tons
                  Total Value: %.2f TND
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