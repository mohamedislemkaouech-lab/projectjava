package tn.agricultureai.domain.exception;

import tn.agricultureai.domain.model.ProductType;
import tn.agricultureai.domain.model.Country;
import java.time.LocalDate;

/**
 * Exception thrown when insufficient data is available for prediction.
 * Demonstrates: Exception with statistical context, threshold validation.
 */
public class InsufficientDataException extends AgricultureException {

    private final int availableRecords;
    private final int requiredRecords;
    private final ProductType productType;
    private final Country destination;
    private final LocalDate fromDate;
    private final LocalDate toDate;

    /**
     * Full constructor
     */
    public InsufficientDataException(
            String message,
            String errorCode,
            int availableRecords,
            int requiredRecords,
            ProductType productType,
            Country destination,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        super(
                message,
                errorCode,
                buildContext(availableRecords, requiredRecords, productType, destination, fromDate, toDate)
        );
        this.availableRecords = availableRecords;
        this.requiredRecords = requiredRecords;
        this.productType = productType;
        this.destination = destination;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    /**
     * Static factory: Not enough historical records
     */
    public static InsufficientDataException notEnoughRecords(
            int availableRecords,
            int requiredRecords,
            ProductType productType,
            Country destination
    ) {
        return new InsufficientDataException(
                String.format(
                        "Insufficient historical data: found %d records, require at least %d",
                        availableRecords,
                        requiredRecords
                ),
                "DATA-001",
                availableRecords,
                requiredRecords,
                productType,
                destination,
                null,
                null
        );
    }

    /**
     * Static factory: No data for date range
     */
    public static InsufficientDataException noDataInRange(
            LocalDate fromDate,
            LocalDate toDate,
            ProductType productType,
            Country destination
    ) {
        return new InsufficientDataException(
                String.format(
                        "No data found for date range: %s to %s",
                        fromDate,
                        toDate
                ),
                "DATA-002",
                0,
                1,
                productType,
                destination,
                fromDate,
                toDate
        );
    }

    /**
     * Static factory: Data too old
     */
    public static InsufficientDataException dataTooOld(
            LocalDate latestDate,
            int maxAgeDays,
            ProductType productType
    ) {
        return new InsufficientDataException(
                String.format(
                        "Latest data is from %s, which is older than %d days",
                        latestDate,
                        maxAgeDays
                ),
                "DATA-003",
                0,
                0,
                productType,
                null,
                latestDate,
                LocalDate.now()
        );
    }

    /**
     * Static factory: No data for product-destination combination
     */
    public static InsufficientDataException noDataForCombination(
            ProductType productType,
            Country destination
    ) {
        return new InsufficientDataException(
                String.format(
                        "No historical data found for %s exports to %s",
                        productType.getDisplayName(),
                        destination.getName()
                ),
                "DATA-004",
                0,
                1,
                productType,
                destination,
                null,
                null
        );
    }

    /**
     * Static factory: Data quality too low
     */
    public static InsufficientDataException lowQuality(
            int validRecords,
            int totalRecords,
            double qualityThreshold
    ) {
        return new InsufficientDataException(
                String.format(
                        "Data quality too low: only %d/%d records valid (%.1f%% < %.1f%% threshold)",
                        validRecords,
                        totalRecords,
                        (validRecords * 100.0 / totalRecords),
                        qualityThreshold * 100
                ),
                "DATA-005",
                validRecords,
                totalRecords,
                null,
                null,
                null,
                null
        );
    }

    // Helper method
    private static String buildContext(
            int availableRecords,
            int requiredRecords,
            ProductType productType,
            Country destination,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return String.format(
                "Available=%d, Required=%d, Product=%s, Destination=%s, DateRange=%s to %s",
                availableRecords,
                requiredRecords,
                productType != null ? productType.getCode() : "N/A",
                destination != null ? destination.getIsoCode() : "N/A",
                fromDate != null ? fromDate : "N/A",
                toDate != null ? toDate : "N/A"
        );
    }

    // Getters
    public int getAvailableRecords() {
        return availableRecords;
    }

    public int getRequiredRecords() {
        return requiredRecords;
    }

    public ProductType getProductType() {
        return productType;
    }

    public Country getDestination() {
        return destination;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    /**
     * Calculate data deficit percentage
     */
    public double getDeficitPercentage() {
        if (requiredRecords == 0) return 0.0;
        return ((requiredRecords - availableRecords) * 100.0) / requiredRecords;
    }

    @Override
    public String getCategory() {
        return "DATA";
    }

    @Override
    public ErrorSeverity getSeverity() {
        // No data at all is HIGH, partial data is MEDIUM
        return availableRecords == 0 ? ErrorSeverity.HIGH : ErrorSeverity.MEDIUM;
    }

    @Override
    public boolean isRetryable() {
        // Data issues won't be fixed by immediate retry
        return false;
    }

    @Override
    public String getUserMessage() {
        if (productType != null && destination != null) {
            return String.format(
                    "Not enough historical data available for %s exports to %s to make accurate predictions.",
                    productType.getDisplayName(),
                    destination.getName()
            );
        }
        return "Insufficient historical data available for accurate predictions.";
    }

    @Override
    public String getDetailedMessage() {
        String base = super.getDetailedMessage();
        return String.format(
                "%s\nData Deficit: %.1f%% (need %d more records)",
                base,
                getDeficitPercentage(),
                Math.max(0, requiredRecords - availableRecords)
        );
    }
}