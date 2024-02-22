package ch.usi.si.seart.service;

import ch.usi.si.seart.model.Label;
import ch.usi.si.seart.model.view.LabelView;
import ch.usi.si.seart.repository.LabelRepository;
import ch.usi.si.seart.repository.LabelViewRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.validation.constraints.NotNull;
import java.util.Collection;

public interface LabelService extends NamedEntityService<Label> {

    @Service
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(onConstructor_ = @Autowired)
    class LabelServiceImpl implements LabelService {

        LabelRepository labelRepository;
        LabelViewRepository labelViewRepository;

        Pageable pageable;

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
        public Collection<Label> getRanked() {
            Collection<String> names = labelViewRepository.findAll(pageable).stream()
                    .map(LabelView::getName)
                    .toList();
            return labelRepository.findAllByNameIn(names);
        }
    }
}
