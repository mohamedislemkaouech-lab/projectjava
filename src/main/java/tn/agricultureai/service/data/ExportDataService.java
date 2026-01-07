package tn.agricultureai.service.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.agricultureai.domain.exception.*;
import tn.agricultureai.domain.model.*;
import tn.agricultureai.repository.ExportDataRepository;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class ExportDataService {

    private final ExportDataRepository repository;
    private final DataValidator validator;

    public ExportData createExport(
            ProductType productType,
            String destinationCountry,
            double volume,
            double pricePerTon,
            LocalDate date,
            MarketIndicator indicator
    ) {
        log.info("Creating export: {} to {}", productType, destinationCountry);

        ExportData export = new ExportData(
                date,
                productType,
                pricePerTon,
                volume,
                destinationCountry,
                indicator
        );

        validator.validate(export);
        ExportData saved = repository.save(export);
        log.info("Export created: {} to {} on {}",
                productType, destinationCountry, date);
        return saved;
    }

    public BulkImportResult importBulkData(List<ExportData> exportList) {
        log.info("Starting bulk import of {} records", exportList.size());

        int successCount = 0;
        int failureCount = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < exportList.size(); i++) {
            try {
                ExportData export = exportList.get(i);
                validator.validate(export);
                repository.save(export);
                successCount++;
            } catch (DataValidationException e) {
                failureCount++;
                errors.add(String.format("Record %d: %s", i, e.getMessage()));
                log.warn("Validation failed for record {}: {}", i, e.getMessage());
            } catch (Exception e) {
                failureCount++;
                errors.add(String.format("Record %d: Unexpected error - %s", i, e.getMessage()));
                log.error("Unexpected error for record {}", i, e);
            }
        }

        log.info("Bulk import completed: {} success, {} failures", successCount, failureCount);

        return new BulkImportResult(
                exportList.size(),
                successCount,
                failureCount,
                errors
        );
    }

    public ExportData updateExport(String exportId, ExportData updatedData) {
        log.info("Updating export: {}", exportId);

        Optional<ExportData> existing = repository.findById(exportId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Export not found: " + exportId);
        }

        validator.validate(updatedData);
        return repository.save(updatedData);
    }

    public boolean deleteExport(String exportId) {
        log.info("Deleting export: {}", exportId);
        boolean deleted = repository.deleteById(exportId);

        if (deleted) {
            log.info("Export deleted successfully");
        } else {
            log.warn("Export not found: {}", exportId);
        }

        return deleted;
    }

    public Optional<ExportData> getExportById(String exportId) {
        return repository.findById(exportId);
    }

    public List<ExportData> getExportsByProduct(ProductType productType) {
        log.debug("Finding exports for product: {}", productType);
        return repository.findByProductType(productType);
    }

    public List<ExportData> getExportsByDestination(String countryName) {
        log.debug("Finding exports to: {}", countryName);
        return repository.findAll().stream()
                .filter(export -> export.destinationCountry().equals(countryName))
                .collect(Collectors.toList());
    }

    public List<ExportData> getExportsByDateRange(LocalDate fromDate, LocalDate toDate) {
        log.debug("Finding exports from {} to {}", fromDate, toDate);
        return repository.findByDateRange(fromDate, toDate);
    }

    public List<ExportData> getRecentExports(int days) {
        log.debug("Finding exports from last {} days", days);
        return repository.findRecent(days);
    }

    public List<ExportData> getEuExports() {
        log.debug("Finding EU exports");
        return repository.findEuExports();
    }

    public ProductStatistics calculateProductStatistics(ProductType productType) {
        log.debug("Calculating statistics for: {}", productType);

        List<ExportData> exports = repository.findByProductType(productType);

        if (exports.isEmpty()) {
            return new ProductStatistics(
                    productType, 0, 0.0, 0.0, 0.0, 0.0, 0.0, Map.of()
            );
        }

        DoubleSummaryStatistics priceStats = exports.stream()
                .mapToDouble(ExportData::pricePerTon)
                .summaryStatistics();

        double totalQuantity = exports.stream()
                .mapToDouble(ExportData::volume)
                .sum();

        double totalValue = exports.stream()
                .mapToDouble(ExportData::getTotalValue)
                .sum();

        Map<String, Long> destinations = exports.stream()
                .collect(Collectors.groupingBy(
                        ExportData::destinationCountry,
                        Collectors.counting()
                ));

        return new ProductStatistics(
                productType,
                exports.size(),
                priceStats.getAverage(),
                priceStats.getMin(),
                priceStats.getMax(),
                totalQuantity,
                totalValue,
                destinations
        );
    }

    public DestinationStatistics calculateDestinationStatistics(String destination) {
        log.debug("Calculating statistics for: {}", destination);

        List<ExportData> exports = repository.findAll().stream()
                .filter(export -> export.destinationCountry().equals(destination))
                .collect(Collectors.toList());

        if (exports.isEmpty()) {
            return new DestinationStatistics(
                    destination, 0, 0.0, 0.0, Map.of()
            );
        }

        DoubleSummaryStatistics priceStats = exports.stream()
                .mapToDouble(ExportData::pricePerTon)
                .summaryStatistics();

        double totalValue = exports.stream()
                .mapToDouble(ExportData::getTotalValue)
                .sum();

        Map<ProductType, Long> products = exports.stream()
                .collect(Collectors.groupingBy(
                        ExportData::productType,
                        Collectors.counting()
                ));

        return new DestinationStatistics(
                destination,
                exports.size(),
                priceStats.getAverage(),
                totalValue,
                products
        );
    }

    public List<ProductValueSummary> getTopProductsByValue(int limit) {
        log.debug("Finding top {} products by value", limit);

        Map<ProductType, Double> valueByProduct = repository.getTotalValueByProduct();

        return valueByProduct.entrySet().stream()
                .sorted(Map.Entry.<ProductType, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> new ProductValueSummary(
                        entry.getKey(),
                        entry.getValue(),
                        (int) repository.findByProductType(entry.getKey()).size()  // FIXED LINE 243: cast to int
                ))
                .collect(Collectors.toList());
    }

    public List<DestinationValueSummary> getTopDestinationsByValue(int limit) {
        log.debug("Finding top {} destinations by value", limit);

        Map<String, Double> valueByDestination = repository.getTotalValueByDestination();

        return valueByDestination.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> new DestinationValueSummary(
                        entry.getKey(),
                        entry.getValue(),
                        (int) repository.findAll().stream()  // FIXED: cast to int
                                .filter(export -> export.destinationCountry().equals(entry.getKey()))
                                .count()
                ))
                .collect(Collectors.toList());
    }

    public List<ExportData> searchExports(SearchCriteria criteria) {
        log.debug("Searching exports with criteria: {}", criteria);

        return repository.findAll().stream()
                .filter(export -> {
                    if (criteria.productType() != null &&
                            !export.productType().equals(criteria.productType())) {
                        return false;
                    }
                    if (criteria.destination() != null &&
                            !export.destinationCountry().equals(criteria.destination())) {
                        return false;
                    }
                    if (criteria.minPrice() != null &&
                            export.pricePerTon() < criteria.minPrice()) {
                        return false;
                    }
                    if (criteria.maxPrice() != null &&
                            export.pricePerTon() > criteria.maxPrice()) {
                        return false;
                    }
                    if (criteria.fromDate() != null &&
                            export.date().isBefore(criteria.fromDate())) {
                        return false;
                    }
                    if (criteria.toDate() != null &&
                            export.date().isAfter(criteria.toDate())) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    public record BulkImportResult(
            int totalRecords,
            int successCount,
            int failureCount,
            List<String> errors
    ) {
        public double getSuccessRate() {
            return totalRecords > 0 ? (successCount * 100.0 / totalRecords) : 0.0;
        }
    }

    public record ProductStatistics(
            ProductType productType,
            int exportCount,
            double averagePrice,
            double minPrice,
            double maxPrice,
            double totalQuantity,
            double totalValue,
            Map<String, Long> destinationDistribution
    ) {}

    public record DestinationStatistics(
            String destination,
            int exportCount,
            double averagePrice,
            double totalValue,
            Map<ProductType, Long> productDistribution
    ) {}

    public record ProductValueSummary(
            ProductType productType,
            double totalValue,
            int exportCount
    ) {}

    public record DestinationValueSummary(
            String destination,
            double totalValue,
            int exportCount
    ) {}

    public record SearchCriteria(
            ProductType productType,
            String destination,
            Double minPrice,
            Double maxPrice,
            LocalDate fromDate,
            LocalDate toDate
    ) {}
}