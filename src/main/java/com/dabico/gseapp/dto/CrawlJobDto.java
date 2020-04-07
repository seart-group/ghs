package com.dabico.gseapp.dto;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
public class CrawlJobDto {
    Long id;
    Date crawled;
    SupportedLanguageDto language;
}
