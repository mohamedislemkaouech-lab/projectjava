package tn.agricultureai.service.factory;

import tn.agricultureai.service.predection.PredictionService;
import tn.agricultureai.service.predection.SamplePredictionService;
import tn.agricultureai.service.predection.DJLPredictionService;
import tn.agricultureai.service.report.*;
import java.util.logging.Logger;

/**
 * Factory for creating service instances.
 * Demonstrates: Factory pattern, singleton pattern.
 *
 * @author Your Name
 */
public class ServiceFactory {

    private static final Logger log = Logger.getLogger(ServiceFactory.class.getName());

    // Singleton instances (lazy initialization)
    private static PredictionService simplePredictionService;
    private static PredictionService djlPredictionService;
    private static ReportGenerator reportGenerator;

    // Private constructor - utility class
    private ServiceFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }

    /**
     * Enum for prediction service types
     */
    public enum PredictionServiceType {
        SIMPLE,
        DJL,
        ONNX
    }

    /**
     * Get prediction service instance
     * Demonstrates: Factory method pattern
     *
     * @param type Service type to create
     * @return Prediction service instance
     */
    public static PredictionService getPredictionService(PredictionServiceType type) {
        log.fine("Requesting prediction service: " + type);

        return switch (type) {
            case SIMPLE -> getSimplePredictionService();
            case DJL -> getDJLPredictionService();
            case ONNX -> getSimplePredictionService(); // Fallback to simple for exam
        };
    }

    /**
     * Get default prediction service (Simple)
     */
    public static PredictionService getDefaultPredictionService() {
        return getSimplePredictionService();
    }

    /**
     * Get simple prediction service (singleton)
     */
    public static synchronized PredictionService getSimplePredictionService() {
        if (simplePredictionService == null) {
            log.info("Creating SamplePredictionService instance");
            simplePredictionService = new SamplePredictionService();
        }
        return simplePredictionService;
    }

    /**
     * Get DJL prediction service (singleton)
     */
    public static synchronized PredictionService getDJLPredictionService() {
        if (djlPredictionService == null) {
            log.info("Creating DJLPredictionService instance");
            djlPredictionService = new DJLPredictionService();
        }
        return djlPredictionService;
    }

    /**
     * Get report generator service (singleton)
     */
    public static synchronized ReportGenerator getReportGenerator() {
        if (reportGenerator == null) {
            log.info("Creating LangChainReportService instance");
            reportGenerator = new LangChainReportService();
        }
        return reportGenerator;
    }

    /**
     * Create new prediction service instance (not singleton)
     * Useful for testing or when fresh instance needed
     */
    public static PredictionService createNewPredictionService(PredictionServiceType type) {
        log.fine("Creating new prediction service instance: " + type);

        return (PredictionService) switch (type) {
            case SIMPLE -> new SamplePredictionService();
            case DJL -> new DJLPredictionService();
            case ONNX -> new SamplePredictionService();
        };
    }

    /**
     * Create new report generator instance
     */
    public static ReportGenerator createNewReportGenerator() {
        log.fine("Creating new report generator instance");
        return new LangChainReportService();
    }

    /**
     * Get all available prediction service types
     */
    public static PredictionServiceType[] getAvailableServiceTypes() {
        return PredictionServiceType.values();
    }

    /**
     * Check if a service type is available
     */
    public static boolean isServiceAvailable(PredictionServiceType type) {
        try {
            PredictionService service = getPredictionService(type);
            return service.isReady();
        } catch (Exception e) {
            log.severe("Service " + type + " not available: " + e.getMessage());
            return false;
        }
    }
}