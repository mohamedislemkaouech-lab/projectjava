package tn.agricultureai.domain.annotation;

import java.lang.annotation.*;

/**
 * Annotation to mark AI/ML model classes.
 * Demonstrates: Custom annotation with parameters, metadata.
 *
 * Usage Example:
 * @AIModel(
 *     name = "OliveOilPricePredictor",
 *     version = "1.0",
 *     framework = AIModel.Framework.ONNX,
 *     accuracy = 0.89
 * )
 * public class OliveOilPredictionService { ... }
 *
 * @author Your Name
 */
@Retention(RetentionPolicy.RUNTIME)  // Available at runtime via reflection
@Target(ElementType.TYPE)             // Can only be applied to classes/interfaces
@Documented                           // Included in JavaDoc
public @interface AIModel {

    /**
     * Name of the AI model
     */
    String name();

    /**
     * Version of the model
     */
    String version() default "1.0";

    /**
     * AI framework used
     */
    Framework framework() default Framework.CUSTOM;

    /**
     * Model accuracy (0.0 to 1.0)
     */
    double accuracy() default 0.0;

    /**
     * Description of what the model does
     */
    String description() default "";

    /**
     * Whether the model is production-ready
     */
    boolean productionReady() default false;

    /**
     * Training date in ISO format (YYYY-MM-DD)
     */
    String trainedDate() default "";

    /**
     * Author/creator of the model
     */
    String author() default "Unknown";

    /**
     * Enum for AI frameworks
     */
    enum Framework {
        DJL("Deep Java Library"),
        ONNX("ONNX Runtime"),
        TENSORFLOW("TensorFlow Java"),
        PYTORCH("PyTorch"),
        CUSTOM("Custom Implementation");

        private final String displayName;

        Framework(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}