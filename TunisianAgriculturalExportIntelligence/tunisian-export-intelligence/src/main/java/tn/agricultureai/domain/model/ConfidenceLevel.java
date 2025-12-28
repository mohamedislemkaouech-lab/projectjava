package tn.agricultureai.domain.model;

/**
 * Enum representing AI prediction confidence levels.
 * Demonstrates: Enum with range validation, factory method.
 */
public enum ConfidenceLevel {
    VERY_LOW("Very Low", 0.0, 0.4, "⚠️"),
    LOW("Low", 0.4, 0.6, "⚡"),
    MEDIUM("Medium", 0.6, 0.75, "✓"),
    HIGH("High", 0.75, 0.9, "✓✓"),
    VERY_HIGH("Very High", 0.9, 1.0, "✓✓✓");

    private final String label;
    private final double minScore;
    private final double maxScore;
    private final String indicator;

    ConfidenceLevel(String label, double minScore, double maxScore, String indicator) {
        this.label = label;
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.indicator = indicator;
    }

    public String getLabel() {
        return label;
    }

    public double getMinScore() {
        return minScore;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public String getIndicator() {
        return indicator;
    }

    /**
     * Factory method: Determine confidence level from score.
     * Demonstrates: Factory pattern in enum, validation logic.
     *
     * @param score Confidence score between 0.0 and 1.0
     * @return Corresponding ConfidenceLevel
     * @throws IllegalArgumentException if score out of range
     */
    public static ConfidenceLevel fromScore(double score) {
        if (score < 0.0 || score > 1.0) {
            throw new IllegalArgumentException(
                    "Confidence score must be between 0.0 and 1.0, got: " + score
            );
        }

        for (ConfidenceLevel level : values()) {
            if (score >= level.minScore && score <= level.maxScore) {
                return level;
            }
        }

        // Fallback (should never happen with proper ranges)
        return MEDIUM;
    }

    /**
     * Check if confidence is acceptable for decisions (>= MEDIUM)
     */
    public boolean isAcceptable() {
        return this.ordinal() >= MEDIUM.ordinal();
    }

    @Override
    public String toString() {
        return String.format("%s %s [%.1f-%.1f]",
                indicator, label, minScore, maxScore);
    }
}