package com.dabico.gseapp.service;

import com.dabico.gseapp.model.CrawlJob;
import com.dabico.gseapp.model.SupportedLanguage;
import com.dabico.gseapp.repository.CrawlJobRepository;
import com.dabico.gseapp.repository.SupportedLanguageRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(onConstructor_ = @Autowired)
public class CrawlJobServiceImpl implements CrawlJobService {
    CrawlJobRepository crawlJobRepository;
    SupportedLanguageRepository supportedLanguageRepository;

    @Override
    public Date getCrawlDateByLanguage(String language){
        Optional<CrawlJob> crawlJob = crawlJobRepository.findCrawledJobByLanguage(language);
        return crawlJob.map(CrawlJob::getCrawled).orElse(null);
    }

    @Override
    public void updateCrawlDateForLanguage(String language, Date date){
        SupportedLanguage supportedLanguage = supportedLanguageRepository.findByName(language).get();
        Optional<CrawlJob> crawlJobOpt = crawlJobRepository.findByLanguage(supportedLanguage);
        if (crawlJobOpt.isEmpty()){
            crawlJobRepository.save(CrawlJob.builder().language(supportedLanguage).crawled(date).build());
        } else {
            CrawlJob crawlJob = crawlJobOpt.get();
            crawlJob.setCrawled(date);
            crawlJobRepository.save(crawlJob);
        }
    }
}
