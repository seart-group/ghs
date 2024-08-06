package ch.usi.si.seart.converter;

import ch.usi.si.seart.model.join.GitRepoMetric;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;

public class ObjectNodeToGitRepoMetricConverter implements Converter<ObjectNode, GitRepoMetric> {

    @Override
    @NotNull
    public GitRepoMetric convert(@NotNull ObjectNode source) {
        long codeLines = source.get("code").asLong();
        long blankLines = source.get("blank").asLong();
        long commentLines = source.get("comment").asLong();
        return GitRepoMetric.builder()
                .codeLines(codeLines)
                .blankLines(blankLines)
                .commentLines(commentLines)
                .build();
    }
}
