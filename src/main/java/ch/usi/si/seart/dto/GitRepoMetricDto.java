package ch.usi.si.seart.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GitRepoMetricDto {

    String language;
    Long blankLines;
    Long codeLines;
    Long commentLines;
}
