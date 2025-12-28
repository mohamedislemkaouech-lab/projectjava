package tn.agricultureai.repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Abstract in-memory repository implementation using Map.
 * Demonstrates: Abstract class, generics, collections, thread-safety.
 *
 * Uses ConcurrentHashMap for thread-safe operations.
 *
 * @param <T> Entity type
 * @param <ID> ID type
 *
 * @author Your Name
 */
public abstract class InMemoryRepository<T, ID> implements Repository<T, ID> {

    // Thread-safe storage
    protected final Map<ID, T> storage = new ConcurrentHashMap<>();

    /**
     * Abstract method - subclasses must implement how to extract ID from entity.
     * Demonstrates: Template method pattern.
     *
     * @param entity Entity
     * @return Entity ID
     */
    protected abstract ID extractId(T entity);

    @Override
    public T save(T entity) {
        Objects.requireNonNull(entity, "Entity cannot be null");
        ID id = extractId(entity);
        Objects.requireNonNull(id, "Entity ID cannot be null");
        storage.put(id, entity);
        return entity;
    }

    @Override
    public List<T> saveAll(List<T> entities) {
        Objects.requireNonNull(entities, "Entities list cannot be null");
        return entities.stream()
                .map(this::save)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<T> findById(ID id) {
        Objects.requireNonNull(id, "ID cannot be null");
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<T> findAll() {
        // Return copy to prevent external modification
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<T> findBy(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "Predicate cannot be null");

        // Stream API demonstration
        return storage.values().stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(ID id) {
        Objects.requireNonNull(id, "ID cannot be null");
        return storage.containsKey(id);
    }

    @Override
    public long count() {
        return storage.size();
    }

    @Override
    public boolean deleteById(ID id) {
        Objects.requireNonNull(id, "ID cannot be null");
        return storage.remove(id) != null;
    }

    @Override
    public boolean delete(T entity) {
        Objects.requireNonNull(entity, "Entity cannot be null");
        ID id = extractId(entity);
        return deleteById(id);
    }

    @Override
    public void deleteAll() {
        storage.clear();
    }

    @Override
    public Optional<T> findFirst(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "Predicate cannot be null");

        return storage.values().stream()
                .filter(predicate)
                .findFirst();
    }

    /**
     * Additional utility: Find top N entities
     */
    public List<T> findTop(int n, Comparator<T> comparator) {
        return storage.values().stream()
                .sorted(comparator)
                .limit(n)
                .collect(Collectors.toList());
    }

    /**
     * Additional utility: Count entities matching predicate
     */
    public long countBy(Predicate<T> predicate) {
        return storage.values().stream()
                .filter(predicate)
                .count();
    }

    /**
     * Get all IDs
     */
    public Set<ID> getAllIds() {
        return new HashSet<>(storage.keySet());
    }
}