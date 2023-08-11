package ch.usi.si.seart.converter;

import ch.usi.si.seart.dto.GitRepoMetricDto;
import ch.usi.si.seart.model.join.GitRepoMetric;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

public class GitRepoMetricToDtoConverter implements Converter<GitRepoMetric, GitRepoMetricDto> {

    @Override
    @NonNull
    public GitRepoMetricDto convert(@NonNull GitRepoMetric source) {
        return GitRepoMetricDto.builder()
                .language(source.getLanguage().getName())
                .blankLines(source.getBlankLines())
                .codeLines(source.getCodeLines())
                .commentLines(source.getCommentLines())
                .build();
    }
}
