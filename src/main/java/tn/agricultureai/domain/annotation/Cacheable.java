package tn.agricultureai.domain.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * Annotation to mark methods whose results should be cached.
 * Demonstrates: Annotation with enum parameters, time-based configuration.
 *
 * Usage Example:
 * @Cacheable(
 *     key = "prediction",
 *     ttl = 3600,
 *     unit = TimeUnit.SECONDS,
 *     condition = "result.isReliable()"
 * )
 * public PredictionResult predict(ProductType type, Country destination) { ... }
 *
 * @author Your Name
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Cacheable {

    /**
     * Cache key prefix
     */
    String key() default "";

    /**
     * Time-to-live (how long to cache)
     */
    long ttl() default 3600;

    /**
     * Time unit for TTL
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * Cache strategy
     */
    Strategy strategy() default Strategy.LRU;

    /**
     * Maximum cache size (number of entries)
     */
    int maxSize() default 100;

    /**
     * Whether to cache null results
     */
    boolean cacheNull() default false;

    /**
     * Condition (SpEL-like expression) when to cache
     * Example: "result != null", "result.confidence > 0.8"
     */
    String condition() default "";

    /**
     * Cache region/namespace for organizing caches
     */
    String region() default "default";

    /**
     * Whether cache is synchronized (thread-safe)
     */
    boolean sync() default false;

    /**
     * Priority level for cache eviction
     */
    Priority priority() default Priority.NORMAL;

    /**
     * Enum for cache strategies
     */
    enum Strategy {
        LRU("Least Recently Used"),
        LFU("Least Frequently Used"),
        FIFO("First In First Out"),
        LIFO("Last In First Out");

        private final String description;

        Strategy(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Enum for cache priority
     */
    enum Priority {
        LOW(1),
        NORMAL(5),
        HIGH(10),
        CRITICAL(100);

        private final int weight;

        Priority(int weight) {
            this.weight = weight;
        }

        public int getWeight() {
            return weight;
        }
    }
}