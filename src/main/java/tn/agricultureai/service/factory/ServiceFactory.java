package tn.agricultureai.service.factory;

import tn.agricultureai.service.prediction.*;
import tn.agricultureai.service.report.*;
import java.util.logging.Logger;

/**
 * Factory for creating service instances.
 * NOW INCLUDES REAL AI MODEL SUPPORT!
 *
 * @author Your Name
 */
public class ServiceFactory {

    private static final Logger log = Logger.getLogger(ServiceFactory.class.getName());

    // Singleton instances
    private static SamplePredictionService simplePredictionService;
    private static DL4JPredictionService dl4jPredictionService; // REAL AI MODEL
    private static PredictionService djlPredictionService;
    private static ReportGenerator reportGenerator;

    private ServiceFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }

    /**
     * Enum for prediction service types
     */
    public enum PredictionServiceType {
        SIMPLE("Simple Statistical Model"),
        DL4J("DeepLearning4J Neural Network - REAL AI"), // NEW!
        DJL("Deep Java Library"),
        ONNX("ONNX Runtime");

        private final String description;

        PredictionServiceType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Get prediction service instance
     */
    public static PredictionService getPredictionService(PredictionServiceType type) {
        log.fine("Requesting prediction service: " + type);

        return switch (type) {
            case SIMPLE -> getSimplePredictionService();
            case DL4J -> getDL4JPredictionService(); // REAL AI MODEL
            case DJL -> getDJLPredictionService();
            case ONNX -> getSimplePredictionService(); // Fallback
        };
    }

    /**
     * Get REAL AI MODEL - DeepLearning4J Neural Network
     * THIS IS THE ACTUAL WORKING AI MODEL!
     */
    public static synchronized DL4JPredictionService getDL4JPredictionService() {
        if (dl4jPredictionService == null) {
            log.info("🤖 Creating REAL AI Model (DeepLearning4J Neural Network)...");
            dl4jPredictionService = new DL4JPredictionService();
            log.info("✅ REAL AI Model loaded and trained successfully!");
        }
        return dl4jPredictionService;
    }

    /**
     * Get default prediction service (now returns REAL AI MODEL)
     */
    public static PredictionService getDefaultPredictionService() {
        return getDL4JPredictionService(); // Changed to use REAL AI MODEL by default
    }

    /**
     * Get simple prediction service (fallback)
     */
    public static PredictionService getSimplePredictionService() {
        if (simplePredictionService == null) {
            synchronized (ServiceFactory.class) {
                if (simplePredictionService == null) {
                    log.info("Creating Simple Prediction Service");
                    simplePredictionService = new SamplePredictionService();
                }
            }
        }
        return simplePredictionService;
    }

    /**
     * Get DJL prediction service (simulated)
     */
    public static synchronized PredictionService getDJLPredictionService() {
        if (djlPredictionService == null) {
            log.info("Creating DJL Prediction Service (simulated)");
            djlPredictionService = new DJLPredictionService();
        }
        return djlPredictionService;
    }

    /**
     * Get report generator service
     */
    public static synchronized ReportGenerator getReportGenerator() {
        if (reportGenerator == null) {
            log.info("Creating LangChain Report Service");
            reportGenerator = new LangChainReportService();
        }
        return reportGenerator;
    }

    /**
     * Create new prediction service instance
     */
    public static PredictionService createNewPredictionService(PredictionServiceType type) {
        log.fine("Creating new prediction service instance: " + type);

        return (PredictionService) switch (type) {
            case SIMPLE -> new SamplePredictionService();
            case DL4J -> new DL4JPredictionService(); // REAL AI
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

    /**
     * Print all available services
     */
    public static void printAvailableServices() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("  📋 AVAILABLE AI PREDICTION SERVICES");
        System.out.println("=".repeat(70));

        for (PredictionServiceType type : PredictionServiceType.values()) {
            boolean available = isServiceAvailable(type);
            String status = available ? "✅ Available" : "❌ Not Available";
            System.out.printf("  %-10s: %s - %s%n",
                    type.name(),
                    status,
                    type.getDescription());
        }

        System.out.println("=".repeat(70));
    }
}