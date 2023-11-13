package ch.usi.si.seart.repository.support;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ExtendedJpaRepository<T, ID> extends JpaRepositoryImplementation<T, ID>, JpaStreamExecutor<T> {
}
