package usi.si.seart.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import usi.si.seart.collection.Ranges;

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
        parameters.put("commits", Ranges.build(commitsMin, commitsMax));
        parameters.put("contributors", Ranges.build(contributorsMin, contributorsMax));
        parameters.put("issues", Ranges.build(issuesMin, issuesMax));
        parameters.put("pulls", Ranges.build(pullsMin, pullsMax));
        parameters.put("branches", Ranges.build(branchesMin, branchesMax));
        parameters.put("releases", Ranges.build(releasesMin, releasesMax));
        parameters.put("stars", Ranges.build(starsMin, starsMax));
        parameters.put("watchers", Ranges.build(watchersMin, watchersMax));
        parameters.put("forks", Ranges.build(forksMin, forksMax));
        parameters.put("created", Ranges.build(createdMin, createdMax));
        parameters.put("committed", Ranges.build(committedMin, committedMax));
        parameters.put("excludeForks", excludeForks);
        parameters.put("onlyForks", onlyForks);
        parameters.put("hasIssues", hasIssues);
        parameters.put("hasPulls", hasPulls);
        parameters.put("hasWiki", hasWiki);
        parameters.put("hasLicense", hasLicense);
        parameters.put("codeLines", Ranges.build(codeLinesMin, codeLinesMax));
        parameters.put("commentLines", Ranges.build(commentLinesMin, commentLinesMax));
        parameters.put("nonBlankLines", Ranges.build(nonBlankLinesMin, nonBlankLinesMax));

        return parameters;
    }
}
