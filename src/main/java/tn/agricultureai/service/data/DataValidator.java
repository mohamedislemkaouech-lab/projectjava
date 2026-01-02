package tn.agricultureai.service.data;

import tn.agricultureai.domain.exception.DataValidationException;
import tn.agricultureai.domain.model.ExportData;
import tn.agricultureai.domain.model.ProductType;
import tn.agricultureai.domain.model.Country;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Validator for export data.
 * Demonstrates: Validation logic, business rules, error handling.
 *
 * @author Your Name
 */
public class DataValidator {

    /**
     * Validate export data according to business rules
     *
     * @param data the export data to validate
     * @throws DataValidationException if validation fails
     */
    public void validate(ExportData data) {
        List<DataValidationException.ValidationError> errors = new ArrayList<>();

        // 1. Basic field validations
        if (data.productType() == null) {
            errors.add(new DataValidationException.ValidationError(
                    "productType", "Product type cannot be null"
            ));
        }

        if (data.destination() == null) {
            errors.add(new DataValidationException.ValidationError(
                    "destination", "Destination country cannot be null"
            ));
        }

        if (data.exportDate() == null) {
            errors.add(new DataValidationException.ValidationError(
                    "exportDate", "Export date cannot be null"
            ));
        }

        if (data.source() == null || data.source().trim().isEmpty()) {
            errors.add(new DataValidationException.ValidationError(
                    "source", "Source cannot be null or empty"
            ));
        }

        // 2. Numeric validations
        if (data.quantity() <= 0) {
            errors.add(new DataValidationException.ValidationError(
                    "quantity", String.format(
                    "Quantity must be positive (got: %.2f)", data.quantity()
            )
            ));
        }

        if (data.pricePerUnit() <= 0) {
            errors.add(new DataValidationException.ValidationError(
                    "pricePerUnit", String.format(
                    "Price per unit must be positive (got: %.2f)", data.pricePerUnit()
            )
            ));
        }

        if (data.pricePerUnit() > 1000000) {
            errors.add(new DataValidationException.ValidationError(
                    "pricePerUnit", String.format(
                    "Price per unit exceeds maximum allowed (got: %.2f, max: 1,000,000)",
                    data.pricePerUnit()
            )
            ));
        }

        // 3. Business rule: Date cannot be in the future
        if (data.exportDate() != null && data.exportDate().isAfter(LocalDate.now())) {
            errors.add(new DataValidationException.ValidationError(
                    "exportDate", String.format(
                    "Export date cannot be in the future (got: %s)", data.exportDate()
            )
            ));
        }

        // 4. Business rule: Date cannot be too old (more than 10 years)
        if (data.exportDate() != null &&
                data.exportDate().isBefore(LocalDate.now().minusYears(10))) {
            errors.add(new DataValidationException.ValidationError(
                    "exportDate", String.format(
                    "Export date cannot be older than 10 years (got: %s)", data.exportDate()
            )
            ));
        }

        // 5. Business rule: Specific product-country restrictions
        validateProductCountryRestrictions(data, errors);

        // 6. Business rule: Minimum order value
        double totalValue = data.getTotalValue();
        if (totalValue < 100) {
            errors.add(new DataValidationException.ValidationError(
                    "totalValue", String.format(
                    "Total export value must be at least 100 (got: %.2f)", totalValue
            )
            ));
        }

        // 7. Business rule: Maximum single shipment quantity
        if (data.quantity() > 10000) {
            errors.add(new DataValidationException.ValidationError(
                    "quantity", String.format(
                    "Single shipment quantity exceeds maximum (got: %.2f, max: 10,000)",
                    data.quantity()
            )
            ));
        }

        // 8. Business rule: Validate source format
        if (data.source() != null && !isValidSourceFormat(data.source())) {
            errors.add(new DataValidationException.ValidationError(
                    "source", String.format(
                    "Source must be in format 'REGION_CODE:FARM_ID' (got: %s)", data.source()
            )
            ));
        }

        // If there are errors, throw exception
        if (!errors.isEmpty()) {
            throw new DataValidationException(errors);
        }
    }

    /**
     * Validate product-country specific business rules
     */
    private void validateProductCountryRestrictions(
            ExportData data,
            List<DataValidationException.ValidationError> errors) {

        if (data.productType() == null || data.destination() == null) {
            return; // Already handled in basic validations
        }

        // Example business rules:
        // 1. DAIRY products cannot be exported to countries without refrigeration standards
        if (data.productType() == ProductType.DAIRY) {
            List<Country> nonRefrigerationCountries = List.of(
                    Country.SOMALIA, Country.YEMEN, Country.SUDAN
            );
            if (nonRefrigerationCountries.contains(data.destination())) {
                errors.add(new DataValidationException.ValidationError(
                        "destination", String.format(
                        "DAIRY products cannot be exported to %s due to lack of refrigeration standards",
                        data.destination()
                )
                ));
            }
        }

        // 2. GRAINS have quantity restrictions to certain destinations
        if (data.productType() == ProductType.GRAINS) {
            List<Country> restrictedGrainDestinations = List.of(
                    Country.NORTH_KOREA, Country.IRAN, Country.SYRIA
            );
            if (restrictedGrainDestinations.contains(data.destination()) && data.quantity() > 1000) {
                errors.add(new DataValidationException.ValidationError(
                        "quantity", String.format(
                        "GRAINS export to %s cannot exceed 1000 units (got: %.2f)",
                        data.destination(), data.quantity()
                )
                ));
            }
        }

        // 3. FRUITS require phytosanitary certificates for certain destinations
        if (data.productType() == ProductType.FRUITS || data.productType() == ProductType.VEGETABLES) {
            List<Country> phytosanitaryCountries = List.of(
                    Country.USA, Country.CANADA, Country.JAPAN, Country.AUSTRALIA
            );
            if (phytosanitaryCountries.contains(data.destination())) {
                // In a real system, we would check for certificate in the data
                // For now, this is just an example rule structure
            }
        }
    }

    /**
     * Validate source format
     * Expected format: "REGION_CODE:FARM_ID"
     * Example: "EU:FR-12345" or "NA:US-CA-789"
     */
    private boolean isValidSourceFormat(String source) {
        if (source == null || source.trim().isEmpty()) {
            return false;
        }

        // Split by colon
        String[] parts = source.split(":");
        if (parts.length != 2) {
            return false;
        }

        String regionCode = parts[0].trim();
        String farmId = parts[1].trim();

        // Validate region code (2-3 characters, uppercase)
        if (!regionCode.matches("^[A-Z]{2,3}$")) {
            return false;
        }

        // Validate farm ID (alphanumeric with optional hyphens)
        if (!farmId.matches("^[A-Z0-9\\-]{3,20}$")) {
            return false;
        }

        return true;
    }

    /**
     * Quick validation for bulk operations (only basic checks)
     * Returns true if data passes basic validation, false otherwise
     */
    public boolean quickValidate(ExportData data) {
        try {
            // Only check the most critical validations
            if (data.productType() == null) return false;
            if (data.destination() == null) return false;
            if (data.exportDate() == null) return false;
            if (data.quantity() <= 0) return false;
            if (data.pricePerUnit() <= 0) return false;
            if (data.exportDate().isAfter(LocalDate.now())) return false;

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate a batch of export data records
     * Returns list of errors for each invalid record
     */
    public List<DataValidationException.ValidationError> validateBatch(List<ExportData> dataList) {
        List<DataValidationException.ValidationError> allErrors = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i++) {
            ExportData data = dataList.get(i);
            try {
                validate(data);
            } catch (DataValidationException e) {
                // Add index information to errors
                for (DataValidationException.ValidationError error : e.getValidationErrors()) {
                    allErrors.add(new DataValidationException.ValidationError(
                            String.format("record[%d].%s", i, error.field()),
                            String.format("[Record %d] %s", i, error.message())
                    ));
                }
            }
        }

        return allErrors;
    }
}