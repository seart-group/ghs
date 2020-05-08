package com.dabico.gseapp.controller;

import com.dabico.gseapp.converter.GitRepoConverter;
import com.dabico.gseapp.dto.GitRepoDtoList;
import com.dabico.gseapp.dto.GitRepoDtoListPaginated;
import com.dabico.gseapp.service.GitRepoService;
import com.dabico.gseapp.util.FileSystemResourceCustom;
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
    static final Logger logger = LoggerFactory.getLogger(GitRepoController.class);
    static final String filePath = "src/main/resources/temp/";
    static final ObjectMapper om;
    static final XmlMapper xmlm;
    static {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        om = new ObjectMapper();
        om.setDateFormat(df);
        xmlm = new XmlMapper();
        xmlm.setDateFormat(df);
    }

    GitRepoService gitRepoService;
    GitRepoConverter gitRepoConverter;

    @GetMapping("/r/search")
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
            @RequestParam(required = false, defaultValue = "20") Integer pageSize
    ){
        GitRepoDtoListPaginated results = gitRepoService.advancedSearch(name, nameEquals, language, license, label,
                                                                        commitsMin, commitsMax, contributorsMin, contributorsMax,
                                                                        issuesMin, issuesMax, pullsMin, pullsMax,
                                                                        branchesMin, branchesMax, releasesMin, releasesMax,
                                                                        starsMin, starsMax, watchersMin, watchersMax,
                                                                        forksMin, forksMax, createdMin, createdMax,
                                                                        committedMin, committedMax, excludeForks, onlyForks,
                                                                        hasIssues, hasPulls, hasWiki, hasLicense,
                                                                        page, pageSize);
        return ResponseEntity.ok(results);
    }

    @GetMapping(
            value = "/r/download/csv",
            produces = "text/csv"
    )
    public ResponseEntity<?> downloadCSV(
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
            @RequestParam(required = false, defaultValue = "false") Boolean hasLicense
    ){
        GitRepoDtoList repoDtos = gitRepoService.advancedSearch(name, nameEquals, language, license, label, commitsMin,
                                                                commitsMax, contributorsMin, contributorsMax, issuesMin,
                                                                issuesMax, pullsMin, pullsMax, branchesMin, branchesMax,
                                                                releasesMin, releasesMax, starsMin, starsMax, watchersMin,
                                                                watchersMax, forksMin, forksMax, createdMin, createdMax,
                                                                committedMin, committedMax, excludeForks, onlyForks,
                                                                hasIssues, hasPulls, hasWiki, hasLicense);
        String fileName = "results-"+System.currentTimeMillis()+".csv";
        File csv = new File(filePath+fileName);
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(csv.getAbsolutePath()));
            List<String[]> rows = gitRepoConverter.repoDtoListToCSVRowList(repoDtos);
            writer.writeAll(rows);
            writer.close();
        } catch (IOException ex){
            logger.error(ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok()
                             .header("Content-Disposition", "attachment; filename=results.csv")
                             .contentLength(csv.length())
                             .contentType(MediaType.parseMediaType("text/csv"))
                             .body(new FileSystemResourceCustom(csv));
    }

    @GetMapping(
            value = "/r/download/json",
            produces = "text/plain"
    )
    public ResponseEntity<?> downloadJSON(
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
            @RequestParam(required = false, defaultValue = "false") Boolean hasLicense
    ){
        GitRepoDtoList repoDtos = gitRepoService.advancedSearch(name, nameEquals, language, license, label, commitsMin,
                                                                commitsMax, contributorsMin, contributorsMax, issuesMin,
                                                                issuesMax, pullsMin, pullsMax, branchesMin, branchesMax,
                                                                releasesMin, releasesMax, starsMin, starsMax, watchersMin,
                                                                watchersMax, forksMin, forksMax, createdMin, createdMax,
                                                                committedMin, committedMax, excludeForks, onlyForks,
                                                                hasIssues, hasPulls, hasWiki, hasLicense);
        String fileName = "results-"+System.currentTimeMillis()+".json";
        File json = new File(filePath+fileName);
        try {
            om.writeValue(json,repoDtos);
        } catch (IOException ex){
            logger.error(ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok()
                             .header("Content-Disposition", "attachment; filename=results.json")
                             .contentLength(json.length())
                             .contentType(MediaType.parseMediaType("text/plain"))
                             .body(new FileSystemResourceCustom(json));
    }

    @GetMapping(
            value = "/r/download/xml",
            produces = "text/xml"
    )
    public ResponseEntity<?> downloadXML(
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
            @RequestParam(required = false, defaultValue = "false") Boolean hasLicense
    ){
        GitRepoDtoList repoDtos = gitRepoService.advancedSearch(name, nameEquals, language, license, label, commitsMin,
                                                                commitsMax, contributorsMin, contributorsMax, issuesMin,
                                                                issuesMax, pullsMin, pullsMax, branchesMin, branchesMax,
                                                                releasesMin, releasesMax, starsMin, starsMax, watchersMin,
                                                                watchersMax, forksMin, forksMax, createdMin, createdMax,
                                                                committedMin, committedMax, excludeForks, onlyForks,
                                                                hasIssues, hasPulls, hasWiki, hasLicense);
        String fileName = "results-"+System.currentTimeMillis()+".xml";
        File xml = new File(filePath+fileName);
        try {
            xmlm.writeValue(xml,repoDtos);
        } catch (IOException ex){
            logger.error(ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok()
                             .header("Content-Disposition", "attachment; filename=results.xml")
                             .contentLength(xml.length())
                             .contentType(MediaType.parseMediaType("text/xml"))
                             .body(new FileSystemResourceCustom(xml));
    }

    @GetMapping("/r/{repoId}")
    public ResponseEntity<?> getRepoById(@PathVariable(value = "repoId") Long repoId){
        try {
            return ResponseEntity.ok(gitRepoService.getRepoById(repoId));
        } catch (EntityNotFoundException ex) {
            logger.error(ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/r/labels")
    public ResponseEntity<?> getAllLabels(){
        return ResponseEntity.ok(gitRepoService.getAllLabels());
    }

    @GetMapping("/r/languages")
    public ResponseEntity<?> getAllLanguages(){
        return ResponseEntity.ok(gitRepoService.getAllLanguages());
    }

    @GetMapping("/r/licenses")
    public ResponseEntity<?> getAllLicenses() {
        return ResponseEntity.ok(gitRepoService.getAllLicenses());
    }
}
