package tn.agricultureai.service.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.agricultureai.domain.exception.DataValidationException;
import tn.agricultureai.domain.model.*;
import tn.agricultureai.repository.ExportDataRepository;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class DataService {

    private final ExportDataRepository repository;
    private final DataValidator validator;

    public ExportData addExportData(ExportData data) {
        log.info("Adding export data: {} to {}",
                data.productType(), data.destinationCountry());

        validator.validate(data);
        return repository.save(data);
    }

    public List<ExportData> addBulkExportData(List<ExportData> dataList) {
        log.info("Adding {} export records in bulk", dataList.size());

        List<DataValidationException.ValidationError> errors = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            try {
                validator.validate(dataList.get(i));
            } catch (DataValidationException e) {
                errors.addAll(e.getValidationErrors());
            }
        }

        if (!errors.isEmpty()) {
            throw new DataValidationException(errors);
        }

        return repository.saveAll(dataList);
    }

    public ProductStatistics getProductStatistics(ProductType productType) {
        List<ExportData> exports = repository.findByProductType(productType);

        if (exports.isEmpty()) {
            return new ProductStatistics(
                    productType, 0, 0.0, 0.0, 0.0, 0.0, 0.0, Map.of()
            );
        }

        DoubleSummaryStatistics priceStats = exports.stream()
                .mapToDouble(ExportData::pricePerTon)  // FIXED: pricePerTon not pricePerUnit
                .summaryStatistics();

        double totalQuantity = exports.stream()
                .mapToDouble(ExportData::volume)  // FIXED: volume not quantity
                .sum();

        double totalValue = exports.stream()
                .mapToDouble(ExportData::getTotalValue)
                .sum();

        // FIXED LINE 88: Map String (country name) not Country enum
        Map<String, Long> destinationDistribution = exports.stream()
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
                destinationDistribution
        );
    }

    public Map<ProductType, Double> getMarketShare() {
        Map<ProductType, Double> totalValues = repository.getTotalValueByProduct();
        double grandTotal = totalValues.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        if (grandTotal == 0) {
            return Map.of();
        }

        return totalValues.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (entry.getValue() / grandTotal) * 100
                ));
    }

    public TrendAnalysis analyzeTrend(
            ProductType productType,
            int periodDays
    ) {
        LocalDate cutoffDate = LocalDate.now().minusDays(periodDays);

        List<ExportData> exports = repository.findByDateRange(cutoffDate, LocalDate.now())
                .stream()
                .filter(e -> e.productType().equals(productType))
                .sorted(Comparator.comparing(ExportData::date))  // FIXED: date() not exportDate()
                .toList();

        if (exports.size() < 2) {
            return new TrendAnalysis(
                    productType, TrendDirection.STABLE, 0.0, exports.size()
            );
        }

        double avgPrice = exports.stream()
                .mapToDouble(ExportData::pricePerTon)  // FIXED: pricePerTon not pricePerUnit
                .average()
                .orElse(0.0);

        double firstHalfAvg = exports.stream()
                .limit(exports.size() / 2)
                .mapToDouble(ExportData::pricePerTon)  // FIXED: pricePerTon not pricePerUnit
                .average()
                .orElse(0.0);

        double secondHalfAvg = exports.stream()
                .skip(exports.size() / 2)
                .mapToDouble(ExportData::pricePerTon)  // FIXED: pricePerTon not pricePerUnit
                .average()
                .orElse(0.0);

        double changePercent = ((secondHalfAvg - firstHalfAvg) / firstHalfAvg) * 100;

        TrendDirection direction;
        if (Math.abs(changePercent) < 2) {
            direction = TrendDirection.STABLE;
        } else if (changePercent > 0) {
            direction = TrendDirection.RISING;
        } else {
            direction = TrendDirection.FALLING;
        }

        return new TrendAnalysis(productType, direction, changePercent, exports.size());
    }

    public ProductComparison compareProducts(
            ProductType product1,
            ProductType product2
    ) {
        ProductStatistics stats1 = getProductStatistics(product1);
        ProductStatistics stats2 = getProductStatistics(product2);

        return new ProductComparison(
                product1, product2,
                stats1, stats2,
                stats1.averagePrice() - stats2.averagePrice(),
                stats1.totalValue() - stats2.totalValue()
        );
    }

    public record ProductStatistics(
            ProductType productType,
            int exportCount,
            double averagePrice,
            double minPrice,
            double maxPrice,
            double totalQuantity,
            double totalValue,
            Map<String, Long> destinationDistribution  // FIXED: String not Country
    ) {}

    public enum TrendDirection {
        RISING, FALLING, STABLE
    }

    public record TrendAnalysis(
            ProductType productType,
            TrendDirection direction,
            double changePercent,
            int sampleSize
    ) {}

    public record ProductComparison(
            ProductType product1,
            ProductType product2,
            ProductStatistics stats1,
            ProductStatistics stats2,
            double priceDifference,
            double valueDifference
    ) {}
}