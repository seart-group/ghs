package ch.usi.si.seart.service;

import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 * A common service interface for JPA entities
 * that share a <code>name</code> attribute.
 *
 * @param <E> An entity class that has a "name" (or equivalent) field.
 */
public interface NamedEntityService<E> {

    /**
     * @param name
     * The name value of the entity
     * @return
     * An existing entity that has a certain <code>name</code>,
     * or a new entity if it does not already exist.
     */
    E getOrCreate(@NotNull String name);

    /**
     * @return A collection of all the entities.
     */
    Collection<E> getAll();
}
