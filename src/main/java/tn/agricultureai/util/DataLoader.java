package tn.agricultureai.util;

import tn.agricultureai.domain.model.ExportData;
import tn.agricultureai.domain.model.ProductType;
import tn.agricultureai.domain.model.MarketIndicator;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Enhanced CSV data loader with analytics for 5-year historical data
 */
public class DataLoader {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Load export data from CSV file in resources folder
     */
    public static List<ExportData> loadFromCSV(String filename) {
        List<ExportData> data = new ArrayList<>();

        try (InputStream is = DataLoader.class.getClassLoader()
                .getResourceAsStream(filename);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(is))) {

            if (is == null) {
                throw new IllegalArgumentException(
                        "File not found in resources: " + filename);
            }

            String line;
            boolean isFirstLine = true;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }

                if (line.trim().isEmpty()) continue;

                try {
                    ExportData record = parseCSVLine(line);
                    data.add(record);
                } catch (Exception e) {
                    System.err.println("Warning: Skipping invalid line " +
                            lineNumber + ": " + e.getMessage());
                }
            }

            System.out.println("Successfully loaded " + data.size() +
                    " records from " + filename);

        } catch (Exception e) {
            System.err.println("Error loading CSV: " + e.getMessage());
            e.printStackTrace();
        }

        return data;
    }

    /**
     * Parse CSV line: date,product,price,volume,country,indicator
     */
    private static ExportData parseCSVLine(String line) {
        String[] parts = line.split(",");

        if (parts.length < 6) {
            throw new IllegalArgumentException(
                    "Expected 6 columns, found " + parts.length);
        }

        LocalDate date = LocalDate.parse(parts[0].trim(), DATE_FORMAT);
        ProductType product = ProductType.valueOf(parts[1].trim().toUpperCase());
        double price = Double.parseDouble(parts[2].trim());
        double volume = Double.parseDouble(parts[3].trim());
        String country = parts[4].trim();
        MarketIndicator indicator = MarketIndicator.valueOf(
                parts[5].trim().toUpperCase());

        return new ExportData(date, product, price, volume, country, indicator);
    }

    /**
     * Get data for a specific year
     */
    public static List<ExportData> filterByYear(
            List<ExportData> data, int year) {
        return data.stream()
                .filter(d -> d.date().getYear() == year)
                .collect(Collectors.toList());
    }

    /**
     * Get data for a specific product
     */
    public static List<ExportData> filterByProduct(
            List<ExportData> data, ProductType product) {
        return data.stream()
                .filter(d -> d.productType() == product)
                .collect(Collectors.toList());
    }

    /**
     * Get data for a specific country
     */
    public static List<ExportData> filterByCountry(
            List<ExportData> data, String country) {
        return data.stream()
                .filter(d -> d.destinationCountry().equalsIgnoreCase(country))
                .collect(Collectors.toList());
    }

    /**
     * Get data within date range
     */
    public static List<ExportData> filterByDateRange(
            List<ExportData> data, LocalDate start, LocalDate end) {
        return data.stream()
                .filter(d -> !d.date().isBefore(start) && !d.date().isAfter(end))
                .collect(Collectors.toList());
    }

    /**
     * Calculate yearly statistics
     */
    public static Map<Integer, YearlyStats> getYearlyStatistics(
            List<ExportData> data) {

        Map<Integer, YearlyStats> yearlyStats = new TreeMap<>();

        // Group by year
        Map<Integer, List<ExportData>> byYear = data.stream()
                .collect(Collectors.groupingBy(d -> d.date().getYear()));

        // Calculate stats for each year
        byYear.forEach((year, yearData) -> {
            double avgPrice = yearData.stream()
                    .mapToDouble(ExportData::pricePerTon)
                    .average()
                    .orElse(0.0);

            double totalVolume = yearData.stream()
                    .mapToDouble(ExportData::volume)
                    .sum();

            int recordCount = yearData.size();

            yearlyStats.put(year, new YearlyStats(
                    year, avgPrice, totalVolume, recordCount));
        });

        return yearlyStats;
    }

    /**
     * Calculate product statistics
     */
    public static Map<ProductType, ProductStats> getProductStatistics(
            List<ExportData> data) {

        Map<ProductType, ProductStats> productStats = new HashMap<>();

        // Group by product
        Map<ProductType, List<ExportData>> byProduct = data.stream()
                .collect(Collectors.groupingBy(ExportData::productType));

        // Calculate stats for each product
        byProduct.forEach((product, productData) -> {
            double avgPrice = productData.stream()
                    .mapToDouble(ExportData::pricePerTon)
                    .average()
                    .orElse(0.0);

            double minPrice = productData.stream()
                    .mapToDouble(ExportData::pricePerTon)
                    .min()
                    .orElse(0.0);

            double maxPrice = productData.stream()
                    .mapToDouble(ExportData::pricePerTon)
                    .max()
                    .orElse(0.0);

            double totalVolume = productData.stream()
                    .mapToDouble(ExportData::volume)
                    .sum();

            int recordCount = productData.size();

            productStats.put(product, new ProductStats(
                    product, avgPrice, minPrice, maxPrice,
                    totalVolume, recordCount));
        });

        return productStats;
    }

    /**
     * Get price trend for a product over time
     */
    public static List<PriceTrend> getPriceTrend(
            List<ExportData> data, ProductType product) {

        return data.stream()
                .filter(d -> d.productType() == product)
                .sorted(Comparator.comparing(ExportData::date))
                .map(d -> new PriceTrend(d.date(), d.pricePerTon()))
                .collect(Collectors.toList());
    }

    // Inner class for yearly statistics
    public record YearlyStats(
            int year,
            double avgPrice,
            double totalVolume,
            int recordCount
    ) {
        @Override
        public String toString() {
            return String.format(
                    "Year %d: Avg Price=%.2f TND, Total Volume=%.2f tons, Records=%d",
                    year, avgPrice, totalVolume, recordCount);
        }
    }

    // Inner class for product statistics
    public record ProductStats(
            ProductType product,
            double avgPrice,
            double minPrice,
            double maxPrice,
            double totalVolume,
            int recordCount
    ) {
        @Override
        public String toString() {
            return String.format(
                    "%s: Avg=%.2f TND, Min=%.2f, Max=%.2f, Volume=%.2f tons, Records=%d",
                    product.getFrenchName(), avgPrice, minPrice, maxPrice,
                    totalVolume, recordCount);
        }
    }

    // Inner class for price trend
    public record PriceTrend(
            LocalDate date,
            double price
    ) {}
}