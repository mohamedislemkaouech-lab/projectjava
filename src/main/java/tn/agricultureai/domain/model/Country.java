package tn.agricultureai.domain.model;

public enum Country {
    FRANCE("France", Region.EU, "FR"),
    GERMANY("Germany", Region.EU, "DE"),
    ITALY("Italy", Region.EU, "IT"),
    SPAIN("Spain", Region.EU, "ES"),
    UK("United Kingdom", Region.EU, "UK"),
    USA("United States", Region.AMERICAS, "US"),
    CHINA("China", Region.ASIA, "CN"),
    JAPAN("Japan", Region.ASIA, "JP"),
    UAE("United Arab Emirates", Region.MIDDLE_EAST, "AE"),
    SAUDI_ARABIA("Saudi Arabia", Region.MIDDLE_EAST, "SA"),
    TUNISIA("Tunisia", Region.AFRICA, "TN"),
    LIBYA("Libya", Region.AFRICA, "LY"),
    ALGERIA("Algeria", Region.AFRICA, "DZ"),
    MOROCCO("Morocco", Region.AFRICA, "MA"),
    EGYPT("Egypt", Region.AFRICA, "EG");

    private final String name;
    private final Region region;
    private final String isoCode;

    Country(String name, Region region, String isoCode) {
        this.name = name;
        this.region = region;
        this.isoCode = isoCode;
    }

    public String getName() {
        return name;
    }

    public Region getRegion() {
        return region;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public enum Region {
        EU, AMERICAS, ASIA, MIDDLE_EAST, AFRICA
    }
}