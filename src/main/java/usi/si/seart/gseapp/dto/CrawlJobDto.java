package usi.si.seart.gseapp.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Builder
public class CrawlJobDto {
    Long id;
    Date crawled;
    String language;
}
