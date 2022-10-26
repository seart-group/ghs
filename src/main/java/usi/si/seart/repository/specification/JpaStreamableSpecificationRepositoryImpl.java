package usi.si.seart.repository.specification;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.stream.Stream;

@Repository
@Transactional(readOnly = true)
@AllArgsConstructor(onConstructor_ = @Autowired)
public class JpaStreamableSpecificationRepositoryImpl<T> implements JpaStreamableSpecificationRepository<T> {

    private final EntityManager em;

    @Override
    public Stream<T> stream(Specification<T> specification, Class<T> domainClass) {
        return stream(specification, domainClass, Sort.unsorted());
    }

    @Override
    public Stream<T> stream(Specification<T> specification, Class<T> domainClass, Sort sort) {
        Assert.notNull(domainClass, "Domain class must not be null!");
        Assert.notNull(sort, "Sort must not be null!");

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(domainClass);
        Root<T> root = query.from(domainClass);

        if (specification != null) {
            Predicate predicate = specification.toPredicate(root, query, builder);
            if (predicate != null) {
                query.where(predicate);
            }
        }

        query.select(root);

        if (sort.isSorted()) {
            query.orderBy(QueryUtils.toOrders(sort, root, builder));
        }

        query.select(root);

        return em.createQuery(query).getResultStream();
    }
}
