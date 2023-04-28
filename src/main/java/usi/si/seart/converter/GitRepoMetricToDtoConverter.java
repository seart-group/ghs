package usi.si.seart.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import usi.si.seart.dto.GitRepoMetricDTO;
import usi.si.seart.model.GitRepoMetric;

public class GitRepoMetricToDtoConverter implements Converter<GitRepoMetric, GitRepoMetricDTO> {

    @Override
    @NonNull
    public GitRepoMetricDTO convert(@NonNull GitRepoMetric source) {
        return GitRepoMetricDTO.builder()
                .language(source.getLanguage().getLanguage())
                .totalLines(source.getTotalLines())
                .codeLines(source.getCodeLines())
                .commentLines(source.getCommentLines())
                .build();
    }
}
