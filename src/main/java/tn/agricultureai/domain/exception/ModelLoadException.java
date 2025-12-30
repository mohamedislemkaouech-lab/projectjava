package tn.agricultureai.domain.exception;

import java.nio.file.Path;

/**
 * Exception thrown when AI model fails to load.
 * Demonstrates: Exception with file system context, detailed diagnostics.
 */
public class ModelLoadException extends AgricultureException {

    private final String modelName;
    private final Path modelPath;
    private final ModelType modelType;
    private final String failureReason;

    /**
     * Enum for model types
     */
    public enum ModelType {
        DJL("Deep Java Library"),
        ONNX("ONNX Runtime"),
        TENSORFLOW("TensorFlow Java"),
        CUSTOM("Custom Model");

        private final String displayName;

        ModelType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Full constructor
     */
    public ModelLoadException(
            String message,
            String errorCode,
            Throwable cause,
            String modelName,
            Path modelPath,
            ModelType modelType,
            String failureReason
    ) {
        super(
                message,
                errorCode,
                cause,
                buildContext(modelName, modelPath, modelType)
        );
        this.modelName = modelName;
        this.modelPath = modelPath;
        this.modelType = modelType;
        this.failureReason = failureReason;
    }

    /**
     * Static factory: Model file not found
     */
    public static ModelLoadException fileNotFound(
            String modelName,
            Path modelPath,
            ModelType modelType
    ) {
        return new ModelLoadException(
                String.format("Model file not found: %s", modelPath),
                "MDL-001",
                null,
                modelName,
                modelPath,
                modelType,
                "File does not exist at specified path"
        );
    }

    /**
     * Static factory: Model file corrupted
     */
    public static ModelLoadException corruptedFile(
            String modelName,
            Path modelPath,
            ModelType modelType,
            Throwable cause
    ) {
        return new ModelLoadException(
                String.format("Model file is corrupted or invalid: %s", modelPath),
                "MDL-002",
                cause,
                modelName,
                modelPath,
                modelType,
                "File exists but cannot be read or parsed"
        );
    }

    /**
     * Static factory: Incompatible model version
     */
    public static ModelLoadException incompatibleVersion(
            String modelName,
            ModelType modelType,
            String expectedVersion,
            String actualVersion
    ) {
        return new ModelLoadException(
                String.format(
                        "Model version mismatch: expected %s, got %s",
                        expectedVersion,
                        actualVersion
                ),
                "MDL-003",
                null,
                modelName,
                null,
                modelType,
                String.format("Expected: %s, Actual: %s", expectedVersion, actualVersion)
        );
    }

    /**
     * Static factory: Insufficient memory
     */
    public static ModelLoadException insufficientMemory(
            String modelName,
            ModelType modelType,
            long requiredBytes,
            long availableBytes
    ) {
        return new ModelLoadException(
                String.format(
                        "Insufficient memory to load model: requires %d MB, available %d MB",
                        requiredBytes / (1024 * 1024),
                        availableBytes / (1024 * 1024)
                ),
                "MDL-004",
                null,
                modelName,
                null,
                modelType,
                String.format("Required: %d MB, Available: %d MB",
                        requiredBytes / (1024 * 1024),
                        availableBytes / (1024 * 1024))
        );
    }

    /**
     * Static factory: Missing dependencies
     */
    public static ModelLoadException missingDependencies(
            String modelName,
            ModelType modelType,
            String missingLibrary
    ) {
        return new ModelLoadException(
                String.format("Missing required library: %s", missingLibrary),
                "MDL-005",
                null,
                modelName,
                null,
                modelType,
                "Required library not found: " + missingLibrary
        );
    }

    // Helper method
    private static String buildContext(
            String modelName,
            Path modelPath,
            ModelType modelType
    ) {
        return String.format(
                "Model=%s, Type=%s, Path=%s",
                modelName,
                modelType.name(),
                modelPath != null ? modelPath : "N/A"
        );
    }

    // Getters
    public String getModelName() {
        return modelName;
    }

    public Path getModelPath() {
        return modelPath;
    }

    public ModelType getModelType() {
        return modelType;
    }

    public String getFailureReason() {
        return failureReason;
    }

    @Override
    public String getCategory() {
        return "MODEL";
    }

    @Override
    public ErrorSeverity getSeverity() {
        return ErrorSeverity.CRITICAL;
    }

    @Override
    public boolean isRetryable() {
        // Only file not found might be fixed by retry (if file is being downloaded)
        return hasErrorCode("MDL-001");
    }

    @Override
    public String getUserMessage() {
        String code = getErrorCode();
        return switch (code) {
            case "MDL-001" -> "AI model not available. Please ensure the model files are downloaded.";
            case "MDL-002" -> "AI model file is corrupted. Please re-download the model.";
            case "MDL-003" -> "AI model version is incompatible. Please update the model.";
            case "MDL-004" -> "Insufficient memory to load AI model. Please close other applications.";
            case "MDL-005" -> "Missing required libraries. Please check installation.";
            default -> "Unable to load AI model. Please contact support.";
        };
    }

    @Override
    public String getDetailedMessage() {
        String base = super.getDetailedMessage();
        return base + "\nFailure Reason: " + failureReason;
    }
}