package tn.agricultureai.domain.model;

import java.time.LocalDate;

public record ExportData(
        LocalDate date,
        ProductType productType,
        double pricePerTon,
        double volume,
        String destinationCountry,
        MarketIndicator indicator
) {
    public ExportData {
        if (pricePerTon < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (volume < 0) {
            throw new IllegalArgumentException("Volume cannot be negative");
        }
    }

    // ALIAS METHODS for compatibility with your code
    public LocalDate exportDate() { return date; }
    public double pricePerUnit() { return pricePerTon; }
    public double quantity() { return volume; }
    public String destination() { return destinationCountry; }

    // Helper methods
    public double getTotalValue() {
        return pricePerTon * volume;
    }

    public boolean isHighValueExport() {
        return getTotalValue() > 100000;
    }
}