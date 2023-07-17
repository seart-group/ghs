package usi.si.seart.converter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import usi.si.seart.model.GitRepo;
import usi.si.seart.util.Dates;

public class JsonObjectToGitRepoConverter implements Converter<JsonObject, GitRepo> {

    @Override
    @NonNull
    public GitRepo convert(@NonNull JsonObject source) {
        GitRepo.GitRepoBuilder builder = GitRepo.builder();

        String repoFullName = source.get("full_name").getAsString();

        JsonElement license = source.get("license");
        JsonElement homepage = source.get("homepage");

        builder.name(repoFullName.toLowerCase());
        builder.isFork(source.get("fork").getAsBoolean());
        builder.defaultBranch(source.get("default_branch").getAsString());
        builder.license((license.isJsonNull()) ? null : license.getAsJsonObject()
                .get("name")
                .getAsString()
                .replace("\"", ""));
        builder.stargazers(source.get("stargazers_count").getAsLong());
        builder.forks(source.get("forks_count").getAsLong());
        builder.watchers(source.get("subscribers_count").getAsLong());
        builder.size(source.get("size").getAsLong());
        builder.createdAt(Dates.fromGitDateString(source.get("created_at").getAsString()));
        builder.pushedAt(Dates.fromGitDateString(source.get("pushed_at").getAsString()));
        builder.updatedAt(Dates.fromGitDateString(source.get("updated_at").getAsString()));
        builder.homepage(homepage.isJsonNull() ? null : homepage.getAsString());
        builder.hasWiki(source.get("has_wiki").getAsBoolean());
        builder.isArchived(source.get("archived").getAsBoolean());

        return builder.build();
    }
}
