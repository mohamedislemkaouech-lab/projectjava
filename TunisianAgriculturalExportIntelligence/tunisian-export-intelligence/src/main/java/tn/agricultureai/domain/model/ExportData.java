package tn.agricultureai.domain.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Record representing historical export data.
 * Demonstrates: Record with UUID, LocalDate, complex validation.
 */
public record ExportData(
        String id,
        ProductType productType,
        Country destination,
        double quantity,        // in tons
        double pricePerUnit,    // EUR per kg
        LocalDate exportDate,
        String source           // Data source (e.g., "Customs", "Ministry")
) {
    /**
     * Compact constructor with validation
     */
    public ExportData {
        Objects.requireNonNull(productType, "Product type cannot be null");
        Objects.requireNonNull(destination, "Destination cannot be null");
        Objects.requireNonNull(exportDate, "Export date cannot be null");

        // Auto-generate ID if not provided
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        if (pricePerUnit <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }

        // Check if export date is not in the future
        if (exportDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Export date cannot be in the future");
        }

        // Default source if not provided
        if (source == null || source.isBlank()) {
            source = "Unknown";
        }
    }

    /**
     * Convenience constructor without ID (auto-generated)
     */
    public ExportData(
            ProductType productType,
            Country destination,
            double quantity,
            double pricePerUnit,
            LocalDate exportDate,
            String source
    ) {
        this(null, productType, destination, quantity, pricePerUnit, exportDate, source);
    }

    /**
     * Calculate total value of export (EUR)
     */
    public double getTotalValue() {
        return quantity * 1000 * pricePerUnit; // tons to kg
    }

    /**
     * Check if export is to EU market
     */
    public boolean isEuExport() {
        return destination.getRegion() == Country.Region.EU;
    }

    /**
     * Check if export is recent (within last 30 days)
     */
    public boolean isRecent() {
        return exportDate.isAfter(LocalDate.now().minusDays(30));
    }

    /**
     * Get formatted summary
     */
    public String getSummary() {
        return String.format(
                "[%s] %.2f tons of %s to %s @ %.2f EUR/kg (Total: %.2f EUR)",
                exportDate,
                quantity,
                productType.getDisplayName(),
                destination.getName(),
                pricePerUnit,
                getTotalValue()
        );
    }
}