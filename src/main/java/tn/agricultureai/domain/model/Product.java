package tn.agricultureai.domain.model;

import java.util.Objects;

/**
 * Record representing an agricultural product.
 * Demonstrates: Java record, compact constructor, validation, custom methods.
 *
 * Records automatically generate:
 * - Constructor
 * - Getters (productType(), description(), unit())
 * - equals() and hashCode()
 * - toString()
 */
public record Product(
        ProductType productType,
        String description,
        String unit,
        double minimumPrice,
        double maximumPrice
) {
    /**
     * Compact constructor for validation.
     * Runs BEFORE field assignment.
     */
    public Product {
        Objects.requireNonNull(productType, "Product type cannot be null");
        Objects.requireNonNull(description, "Description cannot be null");
        Objects.requireNonNull(unit, "Unit cannot be null");

        if (description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be blank");
        }

        if (minimumPrice < 0) {
            throw new IllegalArgumentException("Minimum price cannot be negative");
        }

        if (maximumPrice < minimumPrice) {
            throw new IllegalArgumentException(
                    "Maximum price cannot be less than minimum price"
            );
        }
    }

    /**
     * Alternative constructor with defaults.
     * Demonstrates: Constructor overloading in records.
     */
    public Product(ProductType productType, String description) {
        this(
                productType,
                description,
                "kg",
                productType.getAveragePrice() * 0.7,  // 30% below average
                productType.getAveragePrice() * 1.3   // 30% above average
        );
    }

    /**
     * Custom method: Calculate price range
     */
    public double getPriceRange() {
        return maximumPrice - minimumPrice;
    }

    /**
     * Custom method: Check if price is within range
     */
    public boolean isValidPrice(double price) {
        return price >= minimumPrice && price <= maximumPrice;
    }

    /**
     * Custom method: Get formatted display name
     */
    public String getDisplayName() {
        return String.format("%s - %s", productType.getDisplayName(), description);
    }
}