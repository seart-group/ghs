package usi.si.seart.gseapp.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import usi.si.seart.gseapp.dto.CrawlJobDto;
import usi.si.seart.gseapp.model.CrawlJob;

public class CrawlJobToDtoConverter implements Converter<CrawlJob, CrawlJobDto> {

    @Override
    @NonNull
    public CrawlJobDto convert(@NonNull CrawlJob source) {
        return CrawlJobDto.builder()
                .id(source.getId())
                .language(source.getLanguage().getName())
                .crawled(source.getCrawled())
                .build();
    }
}
