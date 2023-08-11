package ch.usi.si.seart.converter;

import ch.usi.si.seart.model.join.GitRepoMetric;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;

public class JsonObjectToGitRepoMetricConverter implements Converter<JsonObject, GitRepoMetric> {

    @Override
    @NotNull
    public GitRepoMetric convert(@NotNull JsonObject source) {
        long codeLines = source.get("code").getAsLong();
        long blankLines = source.get("blank").getAsLong();
        long commentLines = source.get("comment").getAsLong();
        return GitRepoMetric.builder()
                .codeLines(codeLines)
                .blankLines(blankLines)
                .commentLines(commentLines)
                .build();
    }
}
