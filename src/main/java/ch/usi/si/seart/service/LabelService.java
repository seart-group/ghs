package ch.usi.si.seart.service;

import ch.usi.si.seart.model.Label;
import ch.usi.si.seart.repository.LabelRepository;
import ch.usi.si.seart.repository.LabelStatisticsRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Collection;

public interface LabelService extends NamedEntityService<Label> {

    @Service
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(onConstructor_ = @Autowired)
    class LabelServiceImpl implements LabelService {

        LabelRepository labelRepository;
        LabelStatisticsRepository labelStatisticsRepository;

        @Override
        public Label getOrCreate(@NotNull String name) {
            return labelRepository.findByName(name)
                    .orElseGet(() -> labelRepository.save(
                            Label.builder()
                                    .name(name)
                                    .build()
                    ));
        }

        @Override
        public Collection<Label> getAll(Pageable pageable) {
            Pageable adjusted = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.Direction.DESC,
                    Label.Statistics_.COUNT
            );
            return labelStatisticsRepository.findAll(adjusted).stream()
                    .map(Label.Statistics::getLabel)
                    .toList();
        }

        @Override
        public Collection<Label> getByNameContains(String name, Pageable pageable) {
            return labelRepository.findAllByNameContainsOrderByBestMatch(
                    name, PageRequest.of(
                            pageable.getPageNumber(),
                            pageable.getPageSize()
                    )
            ).getContent();
        }
    }
}
