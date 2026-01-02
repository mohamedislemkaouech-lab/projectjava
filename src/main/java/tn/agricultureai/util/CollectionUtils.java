package tn.agricultureai.util;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Utility class for advanced collection operations.
 * Demonstrates: Stream API, functional programming, collection manipulations.
 *
 * @author Your Name
 */
public final class CollectionUtils {

    private CollectionUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Partition a list into chunks of specified size
     * Demonstrates: Stream collect, custom collector
     *
     * @param list List to partition
     * @param size Chunk size
     * @return List of chunks
     */
    public static <T> List<List<T>> partition(List<T> list, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Partition size must be positive");
        }

        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(new ArrayList<>(
                    list.subList(i, Math.min(i + size, list.size()))
            ));
        }
        return partitions;
    }

    /**
     * Group elements by a classifier and count occurrences
     * Demonstrates: Collectors.groupingBy, Collectors.counting
     */
    public static <T, K> Map<K, Long> countBy(
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
     * Find top N elements by a comparator
     * Demonstrates: Stream sorted, limit
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
     * Find elements that satisfy a predicate
     * Demonstrates: Stream filter
     */
    public static <T> List<T> filterBy(
            Collection<T> collection,
            Predicate<T> predicate
    ) {
        return collection.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * Transform collection using a mapper function
     * Demonstrates: Stream map
     */
    public static <T, R> List<R> mapTo(
            Collection<T> collection,
            Function<T, R> mapper
    ) {
        return collection.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    /**
     * Flatten a collection of collections
     * Demonstrates: Stream flatMap
     */
    public static <T> List<T> flatten(Collection<? extends Collection<T>> collections) {
        return collections.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Remove duplicates from list while preserving order
     * Demonstrates: LinkedHashSet usage
     */
    public static <T> List<T> removeDuplicates(List<T> list) {
        return new ArrayList<>(new LinkedHashSet<>(list));
    }

    /**
     * Zip two lists together
     * Demonstrates: Stream indexed operations
     */
    public static <T, U, R> List<R> zip(
            List<T> list1,
            List<U> list2,
            BiFunction<T, U, R> zipper
    ) {
        int minSize = Math.min(list1.size(), list2.size());
        List<R> result = new ArrayList<>(minSize);

        for (int i = 0; i < minSize; i++) {
            result.add(zipper.apply(list1.get(i), list2.get(i)));
        }

        return result;
    }

    /**
     * Calculate frequency distribution
     * Demonstrates: Map operations, frequency counting
     */
    public static <T> Map<T, Integer> frequency(Collection<T> collection) {
        Map<T, Integer> freq = new HashMap<>();
        for (T item : collection) {
            freq.merge(item, 1, Integer::sum);
        }
        return freq;
    }

    /**
     * Check if collection contains any element matching predicate
     * Demonstrates: Stream anyMatch
     */
    public static <T> boolean anyMatch(
            Collection<T> collection,
            Predicate<T> predicate
    ) {
        return collection.stream().anyMatch(predicate);
    }

    /**
     * Check if all elements match predicate
     * Demonstrates: Stream allMatch
     */
    public static <T> boolean allMatch(
            Collection<T> collection,
            Predicate<T> predicate
    ) {
        return collection.stream().allMatch(predicate);
    }

    /**
     * Get first element matching predicate
     * Demonstrates: Stream filter, findFirst, Optional
     */
    public static <T> Optional<T> findFirst(
            Collection<T> collection,
            Predicate<T> predicate
    ) {
        return collection.stream()
                .filter(predicate)
                .findFirst();
    }

    /**
     * Get distinct elements from collection
     * Demonstrates: Stream distinct
     */
    public static <T> List<T> distinct(Collection<T> collection) {
        return collection.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Reverse a list
     */
    public static <T> List<T> reverse(List<T> list) {
        List<T> reversed = new ArrayList<>(list);
        Collections.reverse(reversed);
        return reversed;
    }

    /**
     * Shuffle a list randomly
     */
    public static <T> List<T> shuffle(List<T> list) {
        List<T> shuffled = new ArrayList<>(list);
        Collections.shuffle(shuffled);
        return shuffled;
    }

    /**
     * Merge multiple collections
     * Demonstrates: Stream concat, flatMap
     */
    @SafeVarargs
    public static <T> List<T> merge(Collection<T>... collections) {
        return Arrays.stream(collections)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Get intersection of two collections
     * Demonstrates: Set operations
     */
    public static <T> Set<T> intersection(Collection<T> c1, Collection<T> c2) {
        Set<T> result = new HashSet<>(c1);
        result.retainAll(c2);
        return result;
    }

    /**
     * Get union of two collections
     */
    public static <T> Set<T> union(Collection<T> c1, Collection<T> c2) {
        Set<T> result = new HashSet<>(c1);
        result.addAll(c2);
        return result;
    }

    /**
     * Get difference of two collections (c1 - c2)
     */
    public static <T> Set<T> difference(Collection<T> c1, Collection<T> c2) {
        Set<T> result = new HashSet<>(c1);
        result.removeAll(c2);
        return result;
    }
}