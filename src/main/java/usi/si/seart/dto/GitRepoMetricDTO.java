package usi.si.seart.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GitRepoMetricDTO {

    String language;
    Long totalLines;
    Long codeLines;
    Long commentLines;
}
