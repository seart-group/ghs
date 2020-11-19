package com.dabico.gseapp.db_access_service;

import com.dabico.gseapp.converter.SupportedLanguageConverter;
import com.dabico.gseapp.dto.CrawlJobDto;
import com.dabico.gseapp.dto.CrawlJobDtoList;
import com.dabico.gseapp.dto.SupportedLanguageDto;
import com.dabico.gseapp.model.CrawlJob;
import com.dabico.gseapp.model.SupportedLanguage;
import com.dabico.gseapp.repository.CrawlJobRepository;
import com.dabico.gseapp.repository.SupportedLanguageRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(onConstructor_ = @Autowired)
public class CrawlJobServiceImpl implements CrawlJobService {
    CrawlJobRepository crawlJobRepository;
    SupportedLanguageRepository supportedLanguageRepository;
    SupportedLanguageConverter supportedLanguageConverter;

    @Override
    public CrawlJobDtoList getCompletedJobs(){
        CrawlJobDtoList crawlJobDtoList = CrawlJobDtoList.builder().build();
        List<CrawlJob> crawlJobs = crawlJobRepository.findAll();
        List<CrawlJobDto> crawlJobDtos = new ArrayList<>();
        for (CrawlJob crawlJob : crawlJobs) {
            SupportedLanguageDto slDto = supportedLanguageConverter.fromLanguageToLanguageDto(crawlJob.getLanguage());
            crawlJobDtos.add(CrawlJobDto.builder()
                                        .id(crawlJob.getId())
                                        .language(slDto)
                                        .crawled(crawlJob.getCrawled())
                                        .build());
        }
        crawlJobDtoList.setItems(crawlJobDtos);
        return crawlJobDtoList;
    }

    @Override
    public Date getCrawlDateByLanguage(String language){
        Optional<CrawlJob> crawlJob = crawlJobRepository.findByLanguage(language);
        return crawlJob.map(CrawlJob::getCrawled).orElse(null);
    }

    @Override
    public void updateCrawlDateForLanguage(String language, Date date){
        SupportedLanguage supportedLanguage = supportedLanguageRepository.findByName(language).orElse(null);
        assert supportedLanguage != null;
        Optional<CrawlJob> crawlJobOpt = crawlJobRepository.findByLanguage(supportedLanguage.getName());
        if (crawlJobOpt.isEmpty()){
            crawlJobRepository.save(CrawlJob.builder().language(supportedLanguage).crawled(date).build());
        } else {
            CrawlJob crawlJob = crawlJobOpt.get();
            crawlJob.setCrawled(date);
            crawlJobRepository.save(crawlJob);
        }
    }
}
