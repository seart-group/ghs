package usi.si.seart.gseapp.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class CrawlJobDtoList {
    @Builder.Default
    List<CrawlJobDto> items = new ArrayList<>();
}
