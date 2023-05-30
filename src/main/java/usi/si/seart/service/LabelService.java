package usi.si.seart.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import usi.si.seart.model.Label;
import usi.si.seart.model.view.LabelView;
import usi.si.seart.repository.LabelRepository;
import usi.si.seart.repository.LabelViewRepository;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.stream.Collectors;

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
                    .collect(Collectors.toList());
            return labelRepository.findAllByNameIn(names);
        }
    }
}
