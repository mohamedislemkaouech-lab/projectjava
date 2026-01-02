package tn.agricultureai.util;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Utility class demonstrating advanced Stream API operations.
 * Demonstrates: Stream API mastery, functional programming, collectors.
 *
 * @author Your Name
 */
public final class StreamOperations {

    private StreamOperations() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Partition stream into two groups based on predicate
     * Demonstrates: Collectors.partitioningBy
     */
    public static <T> Map<Boolean, List<T>> partition(
            Collection<T> collection,
            Predicate<T> predicate
    ) {
        return collection.stream()
                .collect(Collectors.partitioningBy(predicate));
    }

    /**
     * Group by classifier and count
     * Demonstrates: Collectors.groupingBy, Collectors.counting
     */
    public static <T, K> Map<K, Long> groupAndCount(
            Collection<T> collection,
            Function<T, K> classifier
    ) {
        return collection.stream()
                .collect(Collectors.groupingBy(
                        classifier,
                        Collectors.counting()
                ));
    }

    /**
     * Group by classifier and sum values
     * Demonstrates: Collectors.summingDouble
     */
    public static <T, K> Map<K, Double> groupAndSum(
            Collection<T> collection,
            Function<T, K> classifier,
            ToDoubleFunction<T> valueExtractor
    ) {
        return collection.stream()
                .collect(Collectors.groupingBy(
                        classifier,
                        Collectors.summingDouble(valueExtractor)
                ));
    }

    /**
     * Group by classifier and calculate average
     * Demonstrates: Collectors.averagingDouble
     */
    public static <T, K> Map<K, Double> groupAndAverage(
            Collection<T> collection,
            Function<T, K> classifier,
            ToDoubleFunction<T> valueExtractor
    ) {
        return collection.stream()
                .collect(Collectors.groupingBy(
                        classifier,
                        Collectors.averagingDouble(valueExtractor)
                ));
    }

    /**
     * Multi-level grouping (nested maps)
     * Demonstrates: Nested Collectors.groupingBy
     */
    public static <T, K1, K2> Map<K1, Map<K2, List<T>>> groupByTwo(
            Collection<T> collection,
            Function<T, K1> classifier1,
            Function<T, K2> classifier2
    ) {
        return collection.stream()
                .collect(Collectors.groupingBy(
                        classifier1,
                        Collectors.groupingBy(classifier2)
                ));
    }

    /**
     * Calculate statistics for numeric stream
     * Demonstrates: DoubleSummaryStatistics
     */
    public static <T> DoubleSummaryStatistics calculateStatistics(
            Collection<T> collection,
            ToDoubleFunction<T> valueExtractor
    ) {
        return collection.stream()
                .mapToDouble(valueExtractor)
                .summaryStatistics();
    }

    /**
     * Find top N elements by comparator
     * Demonstrates: sorted, limit
     */
    public static <T> List<T> topN(
            Collection<T> collection,
            int n,
            Comparator<T> comparator
    ) {
        return collection.stream()
                .sorted(comparator)
                .limit(n)
                .collect(Collectors.toList());
    }

    /**
     * Find bottom N elements by comparator
     */
    public static <T> List<T> bottomN(
            Collection<T> collection,
            int n,
            Comparator<T> comparator
    ) {
        return collection.stream()
                .sorted(comparator.reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    /**
     * Create frequency map
     * Demonstrates: Collectors.groupingBy with counting
     */
    public static <T> Map<T, Long> frequency(Collection<T> collection) {
        return collection.stream()
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ));
    }

    /**
     * Reduce collection to single value
     * Demonstrates: reduce operation
     */
    public static <T> Optional<T> reduce(
            Collection<T> collection,
            BinaryOperator<T> accumulator
    ) {
        return collection.stream().reduce(accumulator);
    }

    /**
     * Reduce with identity value
     */
    public static <T> T reduceWithIdentity(
            Collection<T> collection,
            T identity,
            BinaryOperator<T> accumulator
    ) {
        return collection.stream().reduce(identity, accumulator);
    }

    /**
     * FlatMap example - flatten nested collections
     * Demonstrates: flatMap
     */
    public static <T> List<T> flatten(Collection<? extends Collection<T>> nested) {
        return nested.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Peek for debugging (side effects)
     * Demonstrates: peek operation
     */
    public static <T> List<T> peekAndCollect(
            Collection<T> collection,
            Consumer<T> action
    ) {
        return collection.stream()
                .peek(action)
                .collect(Collectors.toList());
    }

    /**
     * Skip and limit for pagination
     * Demonstrates: skip, limit
     */
    public static <T> List<T> paginate(
            Collection<T> collection,
            int page,
            int pageSize
    ) {
        return collection.stream()
                .skip((long) page * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
    }

    /**
     * Distinct with custom key extractor
     * Demonstrates: Custom distinct using Set
     */
    public static <T, K> List<T> distinctBy(
            Collection<T> collection,
            Function<T, K> keyExtractor
    ) {
        Set<K> seen = new HashSet<>();
        return collection.stream()
                .filter(item -> seen.add(keyExtractor.apply(item)))
                .collect(Collectors.toList());
    }

    /**
     * All match predicate
     * Demonstrates: allMatch
     */
    public static <T> boolean allMatch(
            Collection<T> collection,
            Predicate<T> predicate
    ) {
        return collection.stream().allMatch(predicate);
    }

    /**
     * Any match predicate
     * Demonstrates: anyMatch
     */
    public static <T> boolean anyMatch(
            Collection<T> collection,
            Predicate<T> predicate
    ) {
        return collection.stream().anyMatch(predicate);
    }

    /**
     * None match predicate
     * Demonstrates: noneMatch
     */
    public static <T> boolean noneMatch(
            Collection<T> collection,
            Predicate<T> predicate
    ) {
        return collection.stream().noneMatch(predicate);
    }

    /**
     * Convert to map with key and value extractors
     * Demonstrates: Collectors.toMap
     */
    public static <T, K, V> Map<K, V> toMap(
            Collection<T> collection,
            Function<T, K> keyMapper,
            Function<T, V> valueMapper
    ) {
        return collection.stream()
                .collect(Collectors.toMap(keyMapper, valueMapper));
    }

    /**
     * Convert to map with merge function for duplicate keys
     */
    public static <T, K, V> Map<K, V> toMapWithMerge(
            Collection<T> collection,
            Function<T, K> keyMapper,
            Function<T, V> valueMapper,
            BinaryOperator<V> mergeFunction
    ) {
        return collection.stream()
                .collect(Collectors.toMap(
                        keyMapper,
                        valueMapper,
                        mergeFunction
                ));
    }

    /**
     * Join strings with delimiter
     * Demonstrates: Collectors.joining
     */
    public static String joinStrings(
            Collection<String> collection,
            String delimiter
    ) {
        return collection.stream()
                .collect(Collectors.joining(delimiter));
    }

    /**
     * Join with prefix and suffix
     */
    public static String joinWithWrap(
            Collection<String> collection,
            String delimiter,
            String prefix,
            String suffix
    ) {
        return collection.stream()
                .collect(Collectors.joining(delimiter, prefix, suffix));
    }

    /**
     * Parallel stream processing
     * Demonstrates: parallelStream
     */
    public static <T, R> List<R> parallelMap(
            Collection<T> collection,
            Function<T, R> mapper
    ) {
        return collection.parallelStream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    /**
     * Sum using reduce
     * Demonstrates: reduce for sum
     */
    public static double sum(
            Collection<Double> collection
    ) {
        return collection.stream()
                .reduce(0.0, Double::sum);
    }

    /**
     * Product using reduce
     * Demonstrates: reduce for multiplication
     */
    public static double product(Collection<Double> collection) {
        return collection.stream()
                .reduce(1.0, (a, b) -> a * b);
    }

    /**
     * Conditional collection
     * Demonstrates: filter with complex conditions
     */
    public static <T> List<T> conditionalCollect(
            Collection<T> collection,
            Predicate<T> condition1,
            Predicate<T> condition2
    ) {
        return collection.stream()
                .filter(condition1.and(condition2))
                .collect(Collectors.toList());
    }

    /**
     * Create ranges
     * Demonstrates: IntStream.range
     */
    public static List<Integer> range(int start, int end) {
        return IntStream.range(start, end)
                .boxed()
                .collect(Collectors.toList());
    }

    /**
     * Create range with step
     */
    public static List<Integer> rangeWithStep(int start, int end, int step) {
        return IntStream.iterate(start, i -> i < end, i -> i + step)
                .boxed()
                .collect(Collectors.toList());
    }

    /**
     * Generate infinite stream with limit
     * Demonstrates: Stream.generate
     */
    public static <T> List<T> generate(Supplier<T> supplier, int count) {
        return Stream.generate(supplier)
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * Iterate stream
     * Demonstrates: Stream.iterate
     */
    public static <T> List<T> iterate(T seed, UnaryOperator<T> f, int count) {
        return Stream.iterate(seed, f)
                .limit(count)
                .collect(Collectors.toList());
    }
}