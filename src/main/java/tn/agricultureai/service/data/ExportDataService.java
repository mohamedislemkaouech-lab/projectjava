package tn.agricultureai.service.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.agricultureai.domain.exception.*;
import tn.agricultureai.domain.model.*;
import tn.agricultureai.repository.ExportDataRepository;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing export data operations.
 * Demonstrates: Business logic layer, service pattern, transaction-like operations.
 *
 * @author Your Name
 */
@Slf4j
@RequiredArgsConstructor
public class ExportDataService {

    private final ExportDataRepository repository;
    private final DataValidator validator;

    /**
     * Create new export record with validation
     */
    public ExportData createExport(
            ProductType productType,
            Country destination,
            double quantity,
            double pricePerUnit,
            LocalDate exportDate,
            String source
    ) {
        log.info("Creating export: {} to {}", productType, destination);

        // Create export data
        ExportData export = new ExportData(
                productType,
                destination,
                quantity,
                pricePerUnit,
                exportDate,
                source
        );

        // Validate
        validator.validate(export);

        // Save
        ExportData saved = repository.save(export);
        log.info("Export created with ID: {}", saved.id());

        return saved;
    }

    /**
     * Import bulk export data from external source
     */
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

    /**
     * Update existing export record
     */
    public ExportData updateExport(String exportId, ExportData updatedData) {
        log.info("Updating export: {}", exportId);

        // Check if exists
        Optional<ExportData> existing = repository.findById(exportId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Export not found: " + exportId);
        }

        // Validate updated data
        validator.validate(updatedData);

        // Save (creates new record with same ID)
        return repository.save(updatedData);
    }

    /**
     * Delete export record
     */
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

    /**
     * Get export by ID
     */
    public Optional<ExportData> getExportById(String exportId) {
        return repository.findById(exportId);
    }

    /**
     * Get all exports for a product
     */
    public List<ExportData> getExportsByProduct(ProductType productType) {
        log.debug("Finding exports for product: {}", productType);
        return repository.findByProductType(productType);
    }

    /**
     * Get all exports to a destination
     */
    public List<ExportData> getExportsByDestination(Country destination) {
        log.debug("Finding exports to: {}", destination);
        return repository.findByDestination(destination);
    }

    /**
     * Get exports within date range
     */
    public List<ExportData> getExportsByDateRange(LocalDate fromDate, LocalDate toDate) {
        log.debug("Finding exports from {} to {}", fromDate, toDate);
        return repository.findByDateRange(fromDate, toDate);
    }

    /**
     * Get recent exports (last N days)
     */
    public List<ExportData> getRecentExports(int days) {
        log.debug("Finding exports from last {} days", days);
        return repository.findRecent(days);
    }

    /**
     * Get EU exports only
     */
    public List<ExportData> getEuExports() {
        log.debug("Finding EU exports");
        return repository.findEuExports();
    }

    /**
     * Calculate statistics for a product
     */
    public ProductStatistics calculateProductStatistics(ProductType productType) {
        log.debug("Calculating statistics for: {}", productType);

        List<ExportData> exports = repository.findByProductType(productType);

        if (exports.isEmpty()) {
            return new ProductStatistics(
                    productType, 0, 0.0, 0.0, 0.0, 0.0, 0.0, Map.of()
            );
        }

        DoubleSummaryStatistics priceStats = exports.stream()
                .mapToDouble(ExportData::pricePerUnit)
                .summaryStatistics();

        double totalQuantity = exports.stream()
                .mapToDouble(ExportData::quantity)
                .sum();

        double totalValue = exports.stream()
                .mapToDouble(ExportData::getTotalValue)
                .sum();

        Map<Country, Long> destinations = exports.stream()
                .collect(Collectors.groupingBy(
                        ExportData::destination,
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

    /**
     * Calculate statistics for a destination
     */
    public DestinationStatistics calculateDestinationStatistics(Country destination) {
        log.debug("Calculating statistics for: {}", destination);

        List<ExportData> exports = repository.findByDestination(destination);

        if (exports.isEmpty()) {
            return new DestinationStatistics(
                    destination, 0, 0.0, 0.0, Map.of()
            );
        }

        DoubleSummaryStatistics priceStats = exports.stream()
                .mapToDouble(ExportData::pricePerUnit)
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

    /**
     * Get top products by export value
     */
    public List<ProductValueSummary> getTopProductsByValue(int limit) {
        log.debug("Finding top {} products by value", limit);

        Map<ProductType, Double> valueByProduct = repository.getTotalValueByProduct();

        return valueByProduct.entrySet().stream()
                .sorted(Map.Entry.<ProductType, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> new ProductValueSummary(
                        entry.getKey(),
                        entry.getValue(),
                        repository.findByProductType(entry.getKey()).size()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Get top destinations by export value
     */
    public List<DestinationValueSummary> getTopDestinationsByValue(int limit) {
        log.debug("Finding top {} destinations by value", limit);

        Map<Country, Double> valueByDestination = repository.getTotalValueByDestination();

        return valueByDestination.entrySet().stream()
                .sorted(Map.Entry.<Country, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> new DestinationValueSummary(
                        entry.getKey(),
                        entry.getValue(),
                        repository.findByDestination(entry.getKey()).size()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Search exports by multiple criteria
     */
    public List<ExportData> searchExports(SearchCriteria criteria) {
        log.debug("Searching exports with criteria: {}", criteria);

        return repository.findAll().stream()
                .filter(export -> {
                    if (criteria.productType() != null &&
                            !export.productType().equals(criteria.productType())) {
                        return false;
                    }
                    if (criteria.destination() != null &&
                            !export.destination().equals(criteria.destination())) {
                        return false;
                    }
                    if (criteria.minPrice() != null &&
                            export.pricePerUnit() < criteria.minPrice()) {
                        return false;
                    }
                    if (criteria.maxPrice() != null &&
                            export.pricePerUnit() > criteria.maxPrice()) {
                        return false;
                    }
                    if (criteria.fromDate() != null &&
                            export.exportDate().isBefore(criteria.fromDate())) {
                        return false;
                    }
                    if (criteria.toDate() != null &&
                            export.exportDate().isAfter(criteria.toDate())) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    // Records for results and summaries

    public record BulkImportResult(
            int totalRecords,
            int successCount,
            int failureCount,
            List<String> errors
    ) {
        public double getSuccessRate() {
            return totalRecords > 0 ? (successCount * 100.0 / totalRecords) : 0.0;
        }

        @Override
        public String toString() {
            return String.format(
                    "Bulk Import: %d total, %d success (%.1f%%), %d failures",
                    totalRecords, successCount, getSuccessRate(), failureCount
            );
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
            Map<Country, Long> destinationDistribution
    ) {}

    public record DestinationStatistics(
            Country destination,
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
            Country destination,
            double totalValue,
            int exportCount
    ) {}

    public record SearchCriteria(
            ProductType productType,
            Country destination,
            Double minPrice,
            Double maxPrice,
            LocalDate fromDate,
            LocalDate toDate
    ) {}
}