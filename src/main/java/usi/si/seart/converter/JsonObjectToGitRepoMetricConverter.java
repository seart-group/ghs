package usi.si.seart.converter;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import usi.si.seart.model.join.GitRepoMetric;

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
