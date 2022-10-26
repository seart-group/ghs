package usi.si.seart.repository.specification;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.stream.Stream;

public interface JpaStreamableSpecificationRepository<T> {

    Stream<T> stream(Specification<T> specification, Class<T> domainClass);
    Stream<T> stream(Specification<T> specification, Class<T> domainClass, Sort sort);
}
