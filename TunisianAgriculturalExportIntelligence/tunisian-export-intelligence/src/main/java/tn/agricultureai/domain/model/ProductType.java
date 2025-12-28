package tn.agricultureai.domain.model;

/**
 * Enum representing Tunisian agricultural export products.
 * Demonstrates: Enum with fields, constructor, and methods.
 *
 * @author Your Name
 */
public enum ProductType {
    OLIVE_OIL("Olive Oil", "OIL", 8.5),
    DATES("Dates", "DAT", 12.3),
    CITRUS("Citrus Fruits", "CIT", 5.7),
    TOMATOES("Tomatoes", "TOM", 3.2),
    PEPPERS("Hot Peppers", "PEP", 6.8),
    SEAFOOD("Seafood", "SEA", 15.4);

    private final String displayName;
    private final String code;
    private final double averagePrice; // EUR per kg (baseline)

    /**
     * Enum constructor (implicitly private)
     *
     * @param displayName Human-readable name
     * @param code Short product code
     * @param averagePrice Historical average price in EUR/kg
     */
    ProductType(String displayName, String code, double averagePrice) {
        this.displayName = displayName;
        this.code = code;
        this.averagePrice = averagePrice;
    }

    // Getters
    public String getDisplayName() {
        return displayName;
    }

    public String getCode() {
        return code;
    }

    public double getAveragePrice() {
        return averagePrice;
    }

    /**
     * Find ProductType by code (case-insensitive).
     * Demonstrates: Stream API, Optional
     *
     * @param code Product code (e.g., "OIL")
     * @return ProductType or null if not found
     */
    public static ProductType fromCode(String code) {
        if (code == null) return null;

        for (ProductType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Check if product is high-value (price > 10 EUR/kg)
     *
     * @return true if high-value product
     */
    public boolean isHighValue() {
        return averagePrice > 10.0;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - %.2f EUR/kg", displayName, code, averagePrice);
    }
}