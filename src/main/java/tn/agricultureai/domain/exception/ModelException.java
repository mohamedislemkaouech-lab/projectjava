package tn.agricultureai.domain.exception;

public class ModelException extends EconomicIntelligenceException {
    public ModelException(String message) {
        super(message);
    }

    public ModelException(String message, Throwable cause) {
        super(message, cause);
    }
}