package ch.usi.si.seart.converter;

import ch.usi.si.seart.git.Commit;
import ch.usi.si.seart.util.Dates;
import com.google.gson.JsonObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import java.util.Date;

public class JsonObjectToGitCommitConverter implements Converter<JsonObject, Commit> {

    @Override
    @NonNull
    public Commit convert(@NonNull JsonObject source) {
        String sha = source.getAsJsonPrimitive("sha").getAsString();
        JsonObject commit = source.getAsJsonObject("commit");
        JsonObject committer = commit.getAsJsonObject("committer");
        String gitDateString = committer.getAsJsonPrimitive("date").getAsString();
        Date date = Dates.fromGitDateString(gitDateString);
        return new Commit(sha, date);
    }
}
