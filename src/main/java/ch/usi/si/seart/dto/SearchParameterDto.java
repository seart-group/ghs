package ch.usi.si.seart.dto;

import ch.usi.si.seart.util.Ranges;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Range;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SearchParameterDto {

    private static final ObjectMapper mapper = new ObjectMapper();
    
    String name = "";
    Boolean nameEquals = false;
    String language = "";
    String license = "";
    String label = "";
    String topic = "";
    
    Long commitsMin;
    Long commitsMax;
    Long contributorsMin;
    Long contributorsMax;
    Long issuesMin;
    Long issuesMax;
    Long pullsMin;
    Long pullsMax;
    Long branchesMin;
    Long branchesMax;
    Long releasesMin;
    Long releasesMax;
    Long starsMin;
    Long starsMax;
    Long watchersMin;
    Long watchersMax;
    Long forksMin;
    Long forksMax;

    Long codeLinesMin;
    Long codeLinesMax;
    Long commentLinesMin;
    Long commentLinesMax;
    Long nonBlankLinesMin;
    Long nonBlankLinesMax;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    Date createdMin;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    Date createdMax;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    Date committedMin;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    Date committedMax;
    
    Boolean excludeForks = false;
    Boolean onlyForks = false;
    Boolean hasIssues = false;
    Boolean hasPulls = false;
    Boolean hasWiki = false;
    Boolean hasLicense = false;

    public Map<String, Object> toMap() {
        return mapper.convertValue(this, new TypeReference<>() {
        });
    }

    public Map<String, Object> toParameterMap() {
        Map<String, Object> parameters = new HashMap<>();

        parameters.put("name", name);
        parameters.put("nameEquals", nameEquals);
        parameters.put("language", language);
        parameters.put("license", license);
        parameters.put("label", label);
        parameters.put("topic", topic);
        parameters.put("commits", getCommits());
        parameters.put("contributors", getContributors());
        parameters.put("issues", getIssues());
        parameters.put("pulls", getPulls());
        parameters.put("branches", getBranches());
        parameters.put("releases", getReleases());
        parameters.put("stars", getStars());
        parameters.put("watchers", getWatchers());
        parameters.put("forks", getForks());
        parameters.put("codeLines", getCodeLines());
        parameters.put("commentLines", getCommentLines());
        parameters.put("nonBlankLines", getNonBlankLines());
        parameters.put("created", getCreated());
        parameters.put("committed", getCommitted());
        parameters.put("excludeForks", excludeForks);
        parameters.put("onlyForks", onlyForks);
        parameters.put("hasIssues", hasIssues);
        parameters.put("hasPulls", hasPulls);
        parameters.put("hasWiki", hasWiki);
        parameters.put("hasLicense", hasLicense);

        return parameters;
    }

    @Schema(hidden = true)
    public Range<Long> getCommits() {
        return Ranges.closed(commitsMin, commitsMax);
    }

    @Schema(hidden = true)
    public Range<Long> getContributors() {
        return Ranges.closed(contributorsMin, contributorsMax);
    }

    @Schema(hidden = true)
    public Range<Long> getIssues() {
        return Ranges.closed(issuesMin, issuesMax);
    }

    @Schema(hidden = true)
    public Range<Long> getPulls() {
        return Ranges.closed(pullsMin, pullsMax);
    }

    @Schema(hidden = true)
    public Range<Long> getBranches() {
        return Ranges.closed(branchesMin, branchesMax);
    }

    @Schema(hidden = true)
    public Range<Long> getReleases() {
        return Ranges.closed(releasesMin, releasesMax);
    }

    @Schema(hidden = true)
    public Range<Long> getStars() {
        return Ranges.closed(starsMin, starsMax);
    }

    @Schema(hidden = true)
    public Range<Long> getWatchers() {
        return Ranges.closed(watchersMin, watchersMax);
    }

    @Schema(hidden = true)
    public Range<Long> getForks() {
        return Ranges.closed(forksMin, forksMax);
    }

    @Schema(hidden = true)
    public Range<Date> getCreated() {
        return Ranges.closed(createdMin, createdMax);
    }

    @Schema(hidden = true)
    public Range<Date> getCommitted() {
        return Ranges.closed(committedMin, committedMax);
    }

    @Schema(hidden = true)
    public Range<Long> getCodeLines() {
        return Ranges.closed(codeLinesMin, codeLinesMax);
    }

    @Schema(hidden = true)
    public Range<Long> getCommentLines() {
        return Ranges.closed(commentLinesMin, commentLinesMax);
    }

    @Schema(hidden = true)
    public Range<Long> getNonBlankLines() {
        return Ranges.closed(nonBlankLinesMin, nonBlankLinesMax);
    }
}
