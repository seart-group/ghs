package usi.si.seart.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import usi.si.seart.dto.GitRepoMetricDto;
import usi.si.seart.model.GitRepoMetric;

public class GitRepoMetricToDtoConverter implements Converter<GitRepoMetric, GitRepoMetricDto> {

    @Override
    @NonNull
    public GitRepoMetricDto convert(@NonNull GitRepoMetric source) {
        return GitRepoMetricDto.builder()
                .language(source.getLanguage().getLanguage())
                .totalLines(source.getTotalLines())
                .codeLines(source.getCodeLines())
                .commentLines(source.getCommentLines())
                .build();
    }
}
