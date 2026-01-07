package tn.agricultureai.domain.model;

public enum ProductType {
    OLIVE_OIL("Huile d'olive", "Olive Oil", "OIL"),
    DATES("Dattes", "Dates", "DTS"),
    CITRUS_FRUITS("Agrumes", "Citrus Fruits", "CTR"),
    WHEAT("Blé", "Wheat", "WHT"),
    TOMATOES("Tomates", "Tomatoes", "TMT"),
    PEPPERS("Piments", "Peppers", "PEP");

    private final String frenchName;
    private final String englishName;
    private final String code;

    ProductType(String frenchName, String englishName, String code) {
        this.frenchName = frenchName;
        this.englishName = englishName;
        this.code = code;
    }

    public String getFrenchName() {
        return frenchName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getDisplayName() {
        return englishName;
    }

    public String getCode() {
        return code;
    }

    public double getAveragePrice() {
        switch (this) {
            case OLIVE_OIL: return 3200.0;
            case DATES: return 2200.0;
            case CITRUS_FRUITS: return 1500.0;
            case WHEAT: return 800.0;
            case TOMATOES: return 1200.0;
            case PEPPERS: return 1800.0;
            default: return 1000.0;
        }
    }

    public boolean isHighValue() {
        return this == OLIVE_OIL || this == DATES || this == PEPPERS;
    }
}