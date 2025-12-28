package tn.agricultureai.domain.model;

/**
 * Enum representing export destination countries.
 * Demonstrates: Enum with nested enum, grouping logic.
 */
public enum Country {
    // European Union
    FRANCE("France", "FR", Region.EU),
    ITALY("Italy", "IT", Region.EU),
    SPAIN("Spain", "ES", Region.EU),
    GERMANY("Germany", "DE", Region.EU),

    // North Africa & Middle East
    LIBYA("Libya", "LY", Region.MENA),
    ALGERIA("Algeria", "DZ", Region.MENA),
    SAUDI_ARABIA("Saudi Arabia", "SA", Region.MENA),
    UAE("United Arab Emirates", "AE", Region.MENA),

    // Other
    USA("United States", "US", Region.AMERICAS),
    CANADA("Canada", "CA", Region.AMERICAS);

    private final String name;
    private final String isoCode;
    private final Region region;

    Country(String name, String isoCode, Region region) {
        this.name = name;
        this.isoCode = isoCode;
        this.region = region;
    }

    public String getName() {
        return name;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public Region getRegion() {
        return region;
    }

    /**
     * Nested enum for geographical regions
     */
    public enum Region {
        EU("European Union"),
        MENA("Middle East & North Africa"),
        AMERICAS("Americas"),
        ASIA("Asia");

        private final String displayName;

        Region(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Find country by ISO code
     *
     * @param isoCode Two-letter country code
     * @return Country or null
     */
    public static Country fromIsoCode(String isoCode) {
        if (isoCode == null) return null;

        for (Country country : values()) {
            if (country.isoCode.equalsIgnoreCase(isoCode)) {
                return country;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name + " (" + isoCode + ")";
    }
}