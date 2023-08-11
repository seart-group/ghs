package ch.usi.si.seart.service;

import java.util.Collection;

/**
 * A common service interface for all JPA entities.
 *
 * @param <E> An entity class.
 */
public interface EntityService<E> {

    /**
     * @return A collection of all the entities stored in the database.
     */
    Collection<E> getAll();
}
