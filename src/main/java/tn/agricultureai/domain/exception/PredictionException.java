package tn.agricultureai.domain.exception;

import tn.agricultureai.domain.model.ProductType;
import tn.agricultureai.domain.model.Country;

public class PredictionException extends AgricultureException {

    private final ProductType productType;
    private final Country destination;
    private final String modelName;

    public PredictionException(
            String message,
            String errorCode,
            Throwable cause,
            ProductType productType,
            Country destination,
            String modelName
    ) {
        super(
                message,
                errorCode,
                cause,
                buildContext(productType, destination, modelName)
        );
        this.productType = productType;
        this.destination = destination;
        this.modelName = modelName;
    }

    public PredictionException(
            String message,
            ProductType productType,
            Country destination
    ) {
        this(message, "PRED-001", null, productType, destination, "Unknown");
    }

    public static PredictionException modelLoadFailure(String modelName, Throwable cause) {
        return new PredictionException(
                "Failed to load prediction model: " + modelName,
                "PRED-002",
                cause,
                null,
                null,
                modelName
        );
    }

    public static PredictionException invalidResult(
            String reason,
            ProductType productType,
            Country destination
    ) {
        return new PredictionException(
                "Invalid prediction result: " + reason,
                "PRED-003",
                null,
                productType,
                destination,
                "N/A"
        );
    }

    // FIXED LINES 89-90
    private static String buildContext(
            ProductType productType,
            Country destination,
            String modelName
    ) {
        return String.format(
                "Product=%s, Destination=%s, Model=%s",
                productType != null ? productType.getCode() : "N/A",
                destination != null ? destination.getIsoCode() : "N/A",
                modelName
        );
    }

    public ProductType getProductType() {
        return productType;
    }

    public Country getDestination() {
        return destination;
    }

    public String getModelName() {
        return modelName;
    }

    @Override
    public String getCategory() {
        return "PREDICTION";
    }

    @Override
    public ErrorSeverity getSeverity() {
        return hasErrorCode("PRED-002") ? ErrorSeverity.CRITICAL : ErrorSeverity.HIGH;
    }

    @Override
    public boolean isRetryable() {
        return !hasErrorCode("PRED-002");
    }

    @Override
    public String getUserMessage() {
        if (productType != null && destination != null) {
            return String.format(
                    "Unable to predict prices for %s exports to %s. Please try again later.",
                    productType.getDisplayName(),
                    destination.getName()
            );
        }
        return "Unable to generate price prediction. Please try again later.";
    }
}