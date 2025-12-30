package tn.agricultureai.util;

import tn.agricultureai.domain.annotation.*;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Utility class for processing custom annotations at runtime.
 * Demonstrates: Reflection, annotation processing, introspection.
 *
 * @author Your Name
 */
public final class AnnotationProcessor {

    private AnnotationProcessor() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Extract AIModel annotation metadata from a class.
     * Demonstrates: Reflection, annotation reading.
     *
     * @param clazz Class to inspect
     * @return ModelInfo or null if not annotated
     */
    public static ModelInfo extractModelInfo(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(AIModel.class)) {
            return null;
        }

        AIModel annotation = clazz.getAnnotation(AIModel.class);
        return new ModelInfo(
                annotation.name(),
                annotation.version(),
                annotation.framework(),
                annotation.accuracy(),
                annotation.description(),
                annotation.productionReady(),
                annotation.trainedDate(),
                annotation.author()
        );
    }

    /**
     * Record to hold AI model metadata
     */
    public record ModelInfo(
            String name,
            String version,
            AIModel.Framework framework,
            double accuracy,
            String description,
            boolean productionReady,
            String trainedDate,
            String author
    ) {
        public String toDetailedString() {
            return String.format("""
                Model Information:
                  Name: %s
                  Version: %s
                  Framework: %s
                  Accuracy: %.2f%%
                  Production Ready: %s
                  Trained Date: %s
                  Author: %s
                  Description: %s
                """,
                    name,
                    version,
                    framework.getDisplayName(),
                    accuracy * 100,
                    productionReady ? "Yes" : "No",
                    trainedDate.isEmpty() ? "N/A" : trainedDate,
                    author,
                    description.isEmpty() ? "N/A" : description
            );
        }
    }

    /**
     * Find all methods in a class that are marked as @Cacheable
     */
    public static List<CacheableMethodInfo> findCacheableMethods(Class<?> clazz) {
        List<CacheableMethodInfo> result = new ArrayList<>();

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Cacheable.class)) {
                Cacheable annotation = method.getAnnotation(Cacheable.class);
                result.add(new CacheableMethodInfo(
                        method.getName(),
                        annotation.key(),
                        annotation.ttl(),
                        annotation.unit(),
                        annotation.strategy(),
                        annotation.maxSize(),
                        annotation.region()
                ));
            }
        }

        return result;
    }

    /**
     * Record to hold cacheable method information
     */
    public record CacheableMethodInfo(
            String methodName,
            String cacheKey,
            long ttl,
            java.util.concurrent.TimeUnit unit,
            Cacheable.Strategy strategy,
            int maxSize,
            String region
    ) {
        public long getTtlInSeconds() {
            return unit.toSeconds(ttl);
        }

        @Override
        public String toString() {
            return String.format(
                    "Method: %s | Key: %s | TTL: %ds | Strategy: %s | Region: %s",
                    methodName,
                    cacheKey,
                    getTtlInSeconds(),
                    strategy.name(),
                    region
            );
        }
    }

    /**
     * Find all fields in a class that require validation
     */
    public static List<ValidatedFieldInfo> findValidatedFields(Class<?> clazz) {
        List<ValidatedFieldInfo> result = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Validated.class)) {
                Validated annotation = field.getAnnotation(Validated.class);
                result.add(new ValidatedFieldInfo(
                        field.getName(),
                        field.getType(),
                        annotation.required(),
                        annotation.notNull(),
                        annotation.notEmpty(),
                        annotation.min(),
                        annotation.max(),
                        annotation.pattern(),
                        annotation.message(),
                        annotation.type()
                ));
            }
        }

        return result;
    }

    /**
     * Record to hold validated field information
     */
    public record ValidatedFieldInfo(
            String fieldName,
            Class<?> fieldType,
            boolean required,
            boolean notNull,
            boolean notEmpty,
            double min,
            double max,
            String pattern,
            String message,
            Validated.ValidationType type
    ) {
        public boolean hasRangeValidation() {
            return min != Double.MIN_VALUE || max != Double.MAX_VALUE;
        }

        public boolean hasPatternValidation() {
            return !pattern.isEmpty();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Field: %s (%s)", fieldName, fieldType.getSimpleName()));

            List<String> rules = new ArrayList<>();
            if (required) rules.add("required");
            if (notNull) rules.add("not-null");
            if (notEmpty) rules.add("not-empty");
            if (hasRangeValidation()) rules.add(String.format("range[%.1f-%.1f]", min, max));
            if (hasPatternValidation()) rules.add("pattern");

            if (!rules.isEmpty()) {
                sb.append(" | Rules: ").append(String.join(", ", rules));
            }

            return sb.toString();
        }
    }

    /**
     * Validate all @Validated fields in an object
     * Returns list of validation errors
     */
    public static List<String> validateObject(Object obj) {
        List<String> errors = new ArrayList<>();
        Class<?> clazz = obj.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Validated.class)) {
                Validated annotation = field.getAnnotation(Validated.class);
                field.setAccessible(true);

                try {
                    Object value = field.get(obj);

                    // Check notNull
                    if (annotation.notNull() && value == null) {
                        errors.add(String.format(
                                "%s: %s (field is null)",
                                field.getName(),
                                annotation.message()
                        ));
                        continue;
                    }

                    // Check required
                    if (annotation.required() && value == null) {
                        errors.add(String.format(
                                "%s: %s (field is required)",
                                field.getName(),
                                annotation.message()
                        ));
                        continue;
                    }

                    if (value != null) {
                        // Check numeric range
                        if (value instanceof Number num) {
                            double val = num.doubleValue();
                            if (val < annotation.min() || val > annotation.max()) {
                                errors.add(String.format(
                                        "%s: %s (value %.2f not in range [%.2f, %.2f])",
                                        field.getName(),
                                        annotation.message(),
                                        val,
                                        annotation.min(),
                                        annotation.max()
                                ));
                            }
                        }

                        // Check string patterns
                        if (value instanceof String str) {
                            if (annotation.notEmpty() && str.isEmpty()) {
                                errors.add(String.format(
                                        "%s: %s (string is empty)",
                                        field.getName(),
                                        annotation.message()
                                ));
                            }

                            if (!annotation.pattern().isEmpty() &&
                                    !str.matches(annotation.pattern())) {
                                errors.add(String.format(
                                        "%s: %s (pattern mismatch)",
                                        field.getName(),
                                        annotation.message()
                                ));
                            }
                        }
                    }

                } catch (IllegalAccessException e) {
                    errors.add(String.format(
                            "%s: Cannot access field for validation",
                            field.getName()
                    ));
                }
            }
        }

        return errors;
    }

    /**
     * Print all annotation metadata for a class
     */
    public static void printClassAnnotations(Class<?> clazz) {
        System.out.println("=== Annotation Analysis for: " + clazz.getSimpleName() + " ===\n");

        // Check for @AIModel
        ModelInfo modelInfo = extractModelInfo(clazz);
        if (modelInfo != null) {
            System.out.println(modelInfo.toDetailedString());
        }

        // Check for @Cacheable methods
        List<CacheableMethodInfo> cacheableMethods = findCacheableMethods(clazz);
        if (!cacheableMethods.isEmpty()) {
            System.out.println("Cacheable Methods:");
            cacheableMethods.forEach(info -> System.out.println("  - " + info));
            System.out.println();
        }

        // Check for @Validated fields
        List<ValidatedFieldInfo> validatedFields = findValidatedFields(clazz);
        if (!validatedFields.isEmpty()) {
            System.out.println("Validated Fields:");
            validatedFields.forEach(info -> System.out.println("  - " + info));
            System.out.println();
        }

        if (modelInfo == null && cacheableMethods.isEmpty() && validatedFields.isEmpty()) {
            System.out.println("No custom annotations found.");
        }
    }
}