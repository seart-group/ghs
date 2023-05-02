package usi.si.seart.converter;

import com.google.gson.JsonObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import usi.si.seart.github.GitCommit;
import usi.si.seart.util.Dates;

import java.util.Date;

public class JsonObjectToGitCommitConverter implements Converter<JsonObject, GitCommit> {

    @Override
    @NonNull
    public GitCommit convert(@NonNull JsonObject source) {
        String sha = source.get("sha").getAsString();
        JsonObject commit = source.get("commit").getAsJsonObject();
        JsonObject committer = commit.get("committer").getAsJsonObject();
        String gitDateString = committer.get("date").getAsString();
        Date date = Dates.fromGitDateString(gitDateString);
        return new GitCommit(sha, date);
    }
}
