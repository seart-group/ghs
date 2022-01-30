package usi.si.seart.gseapp.db_access_service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import usi.si.seart.gseapp.model.CrawlJob;
import usi.si.seart.gseapp.model.SupportedLanguage;
import usi.si.seart.gseapp.repository.CrawlJobRepository;
import usi.si.seart.gseapp.repository.SupportedLanguageRepository;

import javax.persistence.EntityNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(onConstructor_ = @Autowired)
public class CrawlJobServiceImpl implements CrawlJobService {

    CrawlJobRepository crawlJobRepository;
    SupportedLanguageRepository supportedLanguageRepository;

    @Override
    public List<CrawlJob> getCompletedJobs(){
        return crawlJobRepository.findAll();
    }

    @Override
    public Date getCrawlDateByLanguage(String language){
        Optional<CrawlJob> crawlJob = crawlJobRepository.findByLanguage(language);
        return crawlJob.map(CrawlJob::getCrawled).orElse(null);
    }

    @Override
    public void updateCrawlDateForLanguage(String language, Date date){
        log.info("Crawling "+language+" repositories secured upto: "+date);
        SupportedLanguage supportedLanguage = supportedLanguageRepository.findByName(language)
                .orElseThrow(EntityNotFoundException::new);
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
