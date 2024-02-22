package ch.usi.si.seart.repository.support;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.util.Assert;

import jakarta.persistence.EntityManager;
import java.util.stream.Stream;

public class ExtendedJpaRepositoryImpl<T, ID>
        extends SimpleJpaRepository<T, ID>
        implements ExtendedJpaRepository<T, ID>
{
    private static final String SORT_MUST_NOT_BE_NULL = "Sort must not be null!";
    private static final String EXAMPLE_MUST_NOT_BE_NULL = "Example must not be null!";
    private static final String ESCAPE_CHARACTER_MUST_NOT_BE_NULL = "EscapeCharacter must not be null!";

    private EscapeCharacter escapeCharacter = EscapeCharacter.DEFAULT;

    public ExtendedJpaRepositoryImpl(Class<T> domainClass, EntityManager em) {
        this(JpaEntityInformationSupport.getEntityInformation(domainClass, em), em);
    }

    public ExtendedJpaRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
    }

    @Override
    public void setEscapeCharacter(@NotNull EscapeCharacter escapeCharacter) {
        super.setEscapeCharacter(escapeCharacter);
        this.escapeCharacter = escapeCharacter;
    }

    @Override
    public Stream<T> streamAll() {
        return getQuery(null, Sort.unsorted()).getResultStream();
    }

    @Override
    public Stream<T> streamAll(@NotNull Sort sort) {
        Assert.notNull(sort, SORT_MUST_NOT_BE_NULL);
        return getQuery(null, sort).getResultStream();
    }

    @Override
    public <S extends T> Stream<S> streamAll(@NotNull Example<S> example) {
        Assert.notNull(example, EXAMPLE_MUST_NOT_BE_NULL);
        Specification<S> specification = (root, query, cb) -> {
            Assert.notNull(escapeCharacter, ESCAPE_CHARACTER_MUST_NOT_BE_NULL);
            return QueryByExamplePredicateBuilder.getPredicate(root, cb, example, escapeCharacter);
        };
        return getQuery(specification, example.getProbeType(), Sort.unsorted()).getResultStream();
    }

    @Override
    public <S extends T> Stream<S> streamAll(@NotNull Example<S> example, @NotNull Sort sort) {
        Assert.notNull(example, EXAMPLE_MUST_NOT_BE_NULL);
        Assert.notNull(sort, SORT_MUST_NOT_BE_NULL);
        Specification<S> specification = (root, query, cb) -> {
            Assert.notNull(escapeCharacter, ESCAPE_CHARACTER_MUST_NOT_BE_NULL);
            return QueryByExamplePredicateBuilder.getPredicate(root, cb, example, escapeCharacter);
        };
        return getQuery(specification, example.getProbeType(), sort).getResultStream();
    }

    @Override
    public Stream<T> streamAll(Specification<T> specification) {
        return getQuery(specification, Sort.unsorted()).getResultStream();
    }

    @Override
    public Stream<T> streamAll(Specification<T> specification, @NotNull Sort sort) {
        Assert.notNull(sort, SORT_MUST_NOT_BE_NULL);
        return getQuery(specification, sort).getResultStream();
    }
}
