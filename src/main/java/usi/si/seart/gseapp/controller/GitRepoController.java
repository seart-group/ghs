package usi.si.seart.gseapp.controller;

import usi.si.seart.gseapp.converter.GitRepoConverter;
import usi.si.seart.gseapp.dto.GitRepoDtoList;
import usi.si.seart.gseapp.dto.GitRepoDtoListPaginated;
import usi.si.seart.gseapp.db_access_service.GitRepoService;
import usi.si.seart.gseapp.util.FileSystemResourceCustom;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.opencsv.CSVWriter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GitRepoController {
    static Logger logger = LoggerFactory.getLogger(GitRepoController.class);
    final static String downloadFolder = "download-temp/";
    static ObjectMapper om;
    static XmlMapper xmlm;
    static {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        om = new ObjectMapper();
        om.setDateFormat(df);
        xmlm = new XmlMapper();
        xmlm.setDateFormat(df);
    }

    GitRepoService gitRepoService;
    GitRepoConverter gitRepoConverter;

    @GetMapping("/api/r/search")
    public ResponseEntity<?> searchRepos(
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(required = false, defaultValue = "false") Boolean nameEquals,
            @RequestParam(required = false, defaultValue = "") String language,
            @RequestParam(required = false, defaultValue = "") String license,
            @RequestParam(required = false, defaultValue = "") String label,
            @RequestParam(required = false) Long commitsMin,
            @RequestParam(required = false) Long commitsMax,
            @RequestParam(required = false) Long contributorsMin,
            @RequestParam(required = false) Long contributorsMax,
            @RequestParam(required = false) Long issuesMin,
            @RequestParam(required = false) Long issuesMax,
            @RequestParam(required = false) Long pullsMin,
            @RequestParam(required = false) Long pullsMax,
            @RequestParam(required = false) Long branchesMin,
            @RequestParam(required = false) Long branchesMax,
            @RequestParam(required = false) Long releasesMin,
            @RequestParam(required = false) Long releasesMax,
            @RequestParam(required = false) Long starsMin,
            @RequestParam(required = false) Long starsMax,
            @RequestParam(required = false) Long watchersMin,
            @RequestParam(required = false) Long watchersMax,
            @RequestParam(required = false) Long forksMin,
            @RequestParam(required = false) Long forksMax,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date createdMin,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date createdMax,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date committedMin,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date committedMax,
            @RequestParam(required = false, defaultValue = "false") Boolean excludeForks,
            @RequestParam(required = false, defaultValue = "false") Boolean onlyForks,
            @RequestParam(required = false, defaultValue = "false") Boolean hasIssues,
            @RequestParam(required = false, defaultValue = "false") Boolean hasPulls,
            @RequestParam(required = false, defaultValue = "false") Boolean hasWiki,
            @RequestParam(required = false, defaultValue = "false") Boolean hasLicense,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Long totalResultsCached
    ){
        GitRepoDtoListPaginated results = gitRepoService.advancedSearch_paginated(name, nameEquals, language, license, label,
                                                                        commitsMin, commitsMax, contributorsMin, contributorsMax,
                                                                        issuesMin, issuesMax, pullsMin, pullsMax,
                                                                        branchesMin, branchesMax, releasesMin, releasesMax,
                                                                        starsMin, starsMax, watchersMin, watchersMax,
                                                                        forksMin, forksMax, createdMin, createdMax,
                                                                        committedMin, committedMax, excludeForks, onlyForks,
                                                                        hasIssues, hasPulls, hasWiki, hasLicense,
                                                                        page, pageSize, totalResultsCached);
        return ResponseEntity.ok(results);
    }

    @GetMapping(value = "/api/r/download/{fileformat}")
    public ResponseEntity<?> downloadResult(@PathVariable("fileformat") String fileformat,
                                  @RequestParam(required = false, defaultValue = "") String name,
                                  @RequestParam(required = false, defaultValue = "false") Boolean nameEquals,
                                  @RequestParam(required = false, defaultValue = "") String language,
                                  @RequestParam(required = false, defaultValue = "") String license,
                                  @RequestParam(required = false, defaultValue = "") String label,
                                  @RequestParam(required = false) Long commitsMin,
                                  @RequestParam(required = false) Long commitsMax,
                                  @RequestParam(required = false) Long contributorsMin,
                                  @RequestParam(required = false) Long contributorsMax,
                                  @RequestParam(required = false) Long issuesMin,
                                  @RequestParam(required = false) Long issuesMax,
                                  @RequestParam(required = false) Long pullsMin,
                                  @RequestParam(required = false) Long pullsMax,
                                  @RequestParam(required = false) Long branchesMin,
                                  @RequestParam(required = false) Long branchesMax,
                                  @RequestParam(required = false) Long releasesMin,
                                  @RequestParam(required = false) Long releasesMax,
                                  @RequestParam(required = false) Long starsMin,
                                  @RequestParam(required = false) Long starsMax,
                                  @RequestParam(required = false) Long watchersMin,
                                  @RequestParam(required = false) Long watchersMax,
                                  @RequestParam(required = false) Long forksMin,
                                  @RequestParam(required = false) Long forksMax,
                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date createdMin,
                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date createdMax,
                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date committedMin,
                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date committedMax,
                                  @RequestParam(required = false, defaultValue = "false") Boolean excludeForks,
                                  @RequestParam(required = false, defaultValue = "false") Boolean onlyForks,
                                  @RequestParam(required = false, defaultValue = "false") Boolean hasIssues,
                                  @RequestParam(required = false, defaultValue = "false") Boolean hasPulls,
                                  @RequestParam(required = false, defaultValue = "false") Boolean hasWiki,
                                  @RequestParam(required = false, defaultValue = "false") Boolean hasLicense)
    {

        if(!fileformat.equals("csv") && !fileformat.equals("json") && !fileformat.equals("xml"))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        GitRepoDtoList repoDtos = gitRepoService.advancedSearch(name, nameEquals, language, license, label, commitsMin,
                commitsMax, contributorsMin, contributorsMax, issuesMin,
                issuesMax, pullsMin, pullsMax, branchesMin, branchesMax,
                releasesMin, releasesMax, starsMin, starsMax, watchersMin,
                watchersMax, forksMin, forksMax, createdMin, createdMax,
                committedMin, committedMax, excludeForks, onlyForks,
                hasIssues, hasPulls, hasWiki, hasLicense);

        String tempFileName = System.currentTimeMillis()+".temp";
        File tempFile = new File(downloadFolder +tempFileName);
        tempFile.getParentFile().mkdirs();

        String mediaType = "", outputFileName="";
        if(fileformat.equals("csv")) {
            mediaType = "text/csv";
            outputFileName = "results.csv";

            // Write to file
            try {
                CSVWriter writer = new CSVWriter(new FileWriter(tempFile.getAbsolutePath()));
                List<String[]> rows = gitRepoConverter.repoDtoListToCSVRowList(repoDtos);
                writer.writeAll(rows);
                writer.close();
            } catch (IOException ex){
                logger.error(ex.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
        else if(fileformat.equals("json")) {
            mediaType = "text/plain";
            outputFileName = "results.json";

            // Write to file
            try {
                om.writeValue(tempFile, repoDtos);
            } catch (IOException ex){
                logger.error(ex.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
        else if(fileformat.equals("xml")) {
            mediaType = "text/xml";
            outputFileName = "results.xml";

            // Write to file
            try {
                xmlm.writeValue(tempFile, repoDtos);
            } catch (IOException ex){
                logger.error(ex.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename="+outputFileName)
                .contentLength(tempFile.length())
                .contentType(MediaType.parseMediaType(mediaType))
                .body(new FileSystemResourceCustom(tempFile));
    }

    @GetMapping("/api/r/{repoId}")
    public ResponseEntity<?> getRepoById(@PathVariable(value = "repoId") Long repoId){
        try {
            return ResponseEntity.ok(gitRepoService.getRepoById(repoId));
        } catch (EntityNotFoundException ex) {
            logger.error(ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/api/r/labels")
    public ResponseEntity<?> getAllLabels(){
        return ResponseEntity.ok(gitRepoService.getAllLabels());
    }

    @GetMapping("/api/r/languages")
    public ResponseEntity<?> getAllLanguages(){
        return ResponseEntity.ok(gitRepoService.getAllLanguages());
    }

    @GetMapping("/api/r/licenses")
    public ResponseEntity<?> getAllLicenses() {
        return ResponseEntity.ok(gitRepoService.getAllLicenses());
    }
}
