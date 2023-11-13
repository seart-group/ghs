package ch.usi.si.seart.repository.support;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.stream.Stream;

public interface JpaStreamExecutor<T> {

    Stream<T> streamAll();

    Stream<T> streamAll(@NonNull Sort sort);

    <S extends T> Stream<S> streamAll(@NonNull Example<S> example);

    <S extends T> Stream<S> streamAll(@NonNull Example<S> example, @NonNull Sort sort);

    Stream<T> streamAll(@Nullable Specification<T> specification);

    Stream<T> streamAll(@Nullable Specification<T> specification, @NonNull Sort sort);
}
