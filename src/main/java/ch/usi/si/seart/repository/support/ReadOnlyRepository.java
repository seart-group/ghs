package ch.usi.si.seart.repository.support;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

/**
 * A read-only {@link Repository} interface.
 * Provides methods for retrieving entities without
 * the possibility of modifying the underlying data source.
 *
 * @param <T> the entity type
 * @param <ID> the entity identifier type
 * @author Ozren Dabić
 */
@NoRepositoryBean
public interface ReadOnlyRepository<T, ID> extends Repository<T, ID> {

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#findAll()
     */
    List<T> findAll();

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.PagingAndSortingRepository#findAll(Sort)
     */
    List<T> findAll(Sort sort);

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.PagingAndSortingRepository#findAll(Pageable)
     */
    Page<T> findAll(Pageable pageable);

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#findById(Object)
     */
    Optional<T> findById(ID id);

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#findAll(Iterable)
     */
    List<T> findAllById(Iterable<ID> ids);

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#existsById(Object)
     */
    boolean existsById(ID id);

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#count()
     */
    long count();
}
