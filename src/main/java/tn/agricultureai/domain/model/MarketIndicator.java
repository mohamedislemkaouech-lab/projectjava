package tn.agricultureai.domain.model;

public enum MarketIndicator {
    STABLE("Stable market conditions"),
    VOLATILE("High price volatility"),
    RISING("Prices trending upward"),
    FALLING("Prices trending downward"),
    UNPREDICTABLE("Highly unpredictable market");

    private final String description;

    MarketIndicator(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}