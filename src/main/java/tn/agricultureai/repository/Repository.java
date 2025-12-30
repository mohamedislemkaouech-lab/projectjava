package tn.agricultureai.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Generic repository interface for data access operations.
 * Demonstrates: Generics, interface design, CRUD operations.
 *
 * @param <T> Entity type
 * @param <ID> ID type
 *
 * @author Your Name
 */
public interface Repository<T, ID> {

    /**
     * Save an entity (insert or update)
     *
     * @param entity Entity to save
     * @return Saved entity
     */
    T save(T entity);

    /**
     * Save multiple entities
     *
     * @param entities Entities to save
     * @return List of saved entities
     */
    List<T> saveAll(List<T> entities);

    /**
     * Find entity by ID
     *
     * @param id Entity ID
     * @return Optional containing entity or empty
     */
    Optional<T> findById(ID id);

    /**
     * Find all entities
     *
     * @return List of all entities
     */
    List<T> findAll();

    /**
     * Find entities matching a predicate
     * Demonstrates: Functional interface usage
     *
     * @param predicate Condition to match
     * @return List of matching entities
     */
    List<T> findBy(Predicate<T> predicate);

    /**
     * Check if entity exists by ID
     *
     * @param id Entity ID
     * @return true if exists
     */
    boolean existsById(ID id);

    /**
     * Count all entities
     *
     * @return Total count
     */
    long count();

    /**
     * Delete entity by ID
     *
     * @param id Entity ID
     * @return true if deleted
     */
    boolean deleteById(ID id);

    /**
     * Delete entity
     *
     * @param entity Entity to delete
     * @return true if deleted
     */
    boolean delete(T entity);

    /**
     * Delete all entities
     */
    void deleteAll();

    /**
     * Find first entity matching predicate
     *
     * @param predicate Condition to match
     * @return Optional containing first match or empty
     */
    Optional<T> findFirst(Predicate<T> predicate);
}