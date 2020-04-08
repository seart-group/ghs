package com.dabico.gseapp.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class CrawlJobDtoList {
    @Builder.Default
    List<CrawlJobDto> items = new ArrayList<>();
}
