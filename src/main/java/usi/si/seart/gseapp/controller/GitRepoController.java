package usi.si.seart.gseapp.controller;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.Range;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import usi.si.seart.gseapp.db_access_service.ApplicationPropertyService;
import usi.si.seart.gseapp.db_access_service.GitRepoService;
import usi.si.seart.gseapp.dto.GitRepoDto;
import usi.si.seart.gseapp.model.GitRepo;
import usi.si.seart.gseapp.util.FileSystemResourceCustom;
import usi.si.seart.gseapp.util.Ranges;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("ConstantConditions")
@Slf4j
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GitRepoController {
    static String downloadFolder = "download-temp/";

    static CsvSchema csvSchema = GitRepoDto.getCsvSchema();
    static CsvMapper csvMapper;
    static ObjectMapper objMapper;
    static XmlMapper xmlMapper;

    static {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        csvMapper = new CsvMapper();
        csvMapper.setDateFormat(df);
        csvMapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        objMapper = new ObjectMapper();
        objMapper.setDateFormat(df);
        xmlMapper = new XmlMapper();
        xmlMapper.setDateFormat(df);
    }

    ConversionService conversionService;
    ApplicationPropertyService applicationPropertyService;
    GitRepoService gitRepoService;

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
            @RequestParam(required = false, defaultValue = "0") Integer page
    ){
        Range<Long> commits = Ranges.build(commitsMin, commitsMax);
        Range<Long> contributors = Ranges.build(contributorsMin, contributorsMax);
        Range<Long> issues = Ranges.build(issuesMin, issuesMax);
        Range<Long> pulls = Ranges.build(pullsMin, pullsMax);
        Range<Long> branches = Ranges.build(branchesMin, branchesMax);
        Range<Long> releases = Ranges.build(releasesMin, releasesMax);
        Range<Long> stars = Ranges.build(starsMin, starsMax);
        Range<Long> watchers = Ranges.build(watchersMin, watchersMax);
        Range<Long> forks = Ranges.build(forksMin, forksMax);
        Range<Date> created = Ranges.build(createdMin, createdMax);
        Range<Date> committed = Ranges.build(committedMin, committedMax);

        Integer pageSize = applicationPropertyService.getPageSize();
        Sort sort = Sort.by("name").ascending();
        PageRequest pageRequest = PageRequest.of(page, pageSize, sort);

        Page<GitRepo> results = gitRepoService.findDynamically(
                name, nameEquals, language, license, label, commits, contributors, issues, pulls, branches, releases,
                stars, watchers, forks, created, committed, excludeForks, onlyForks, hasIssues, hasPulls, hasWiki,
                hasLicense, pageRequest
        );

        List<GitRepoDto> dtos = List.of(
                conversionService.convert(
                        results.getContent().toArray(new GitRepo[0]), GitRepoDto[].class
                )
        );

        long totalItems = results.getTotalElements();
        int totalPages = results.getTotalPages();

        List<String> links = new ArrayList<>();

        if (!results.isFirst()){
            String first = ControllerLinkBuilder.linkTo(
                    ControllerLinkBuilder.methodOn(GitRepoController.class).searchRepos(
                            name, nameEquals, language, license, label, commitsMin, commitsMax, contributorsMin,
                            contributorsMax, issuesMin, issuesMax, pullsMin, pullsMax, branchesMin, branchesMax,
                            releasesMin, releasesMax, starsMin, starsMax, watchersMin, watchersMax, forksMin, forksMax,
                            createdMin, createdMax, committedMin, committedMax, excludeForks, onlyForks, hasIssues,
                            hasPulls, hasWiki, hasLicense, 0
                    )
            ).withRel(Link.REL_FIRST).expand().toString();
            links.add(first);
        }

        if (results.hasPrevious()){
            String prev = ControllerLinkBuilder.linkTo(
                    ControllerLinkBuilder.methodOn(GitRepoController.class).searchRepos(
                            name, nameEquals, language, license, label, commitsMin, commitsMax, contributorsMin,
                            contributorsMax, issuesMin, issuesMax, pullsMin, pullsMax, branchesMin, branchesMax,
                            releasesMin, releasesMax, starsMin, starsMax, watchersMin, watchersMax, forksMin, forksMax,
                            createdMin, createdMax, committedMin, committedMax, excludeForks, onlyForks, hasIssues,
                            hasPulls, hasWiki, hasLicense, page - 1
                    )
            ).withRel(Link.REL_PREVIOUS).expand().toString();
            links.add(prev);
        }

        if (results.hasNext()){
            String next = ControllerLinkBuilder.linkTo(
                    ControllerLinkBuilder.methodOn(GitRepoController.class).searchRepos(
                            name, nameEquals, language, license, label, commitsMin, commitsMax, contributorsMin,
                            contributorsMax, issuesMin, issuesMax, pullsMin, pullsMax, branchesMin, branchesMax,
                            releasesMin, releasesMax, starsMin, starsMax, watchersMin, watchersMax, forksMin, forksMax,
                            createdMin, createdMax, committedMin, committedMax, excludeForks, onlyForks, hasIssues,
                            hasPulls, hasWiki, hasLicense, page + 1
                    )
            ).withRel(Link.REL_NEXT).expand().toString();
            links.add(next);
        }

        if (!results.isLast()){
            String last = ControllerLinkBuilder.linkTo(
                    ControllerLinkBuilder.methodOn(GitRepoController.class).searchRepos(
                            name, nameEquals, language, license, label, commitsMin, commitsMax, contributorsMin,
                            contributorsMax, issuesMin, issuesMax, pullsMin, pullsMax, branchesMin, branchesMax,
                            releasesMin, releasesMax, starsMin, starsMax, watchersMin, watchersMax, forksMin, forksMax,
                            createdMin, createdMax, committedMin, committedMax, excludeForks, onlyForks, hasIssues,
                            hasPulls, hasWiki, hasLicense, totalPages - 1
                    )
            ).withRel(Link.REL_LAST).expand().toString();
            links.add(last);
        }

        String base = ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(GitRepoController.class).searchRepos(
                        name, nameEquals, language, license, label, commitsMin, commitsMax, contributorsMin,
                        contributorsMax, issuesMin, issuesMax, pullsMin, pullsMax, branchesMin, branchesMax,
                        releasesMin, releasesMax, starsMin, starsMax, watchersMin, watchersMax, forksMin, forksMax,
                        createdMin, createdMax, committedMin, committedMax, excludeForks, onlyForks, hasIssues,
                        hasPulls, hasWiki, hasLicense, null
                )
        ).withRel("base").expand().toString();
        links.add(base);

        List<String> download = new ArrayList<>();

        if (totalItems > 0) {
            String csv = ControllerLinkBuilder.linkTo(
                    ControllerLinkBuilder.methodOn(GitRepoController.class).downloadRepos(
                            "csv", name, nameEquals, language, license, label, commitsMin, commitsMax, contributorsMin,
                            contributorsMax, issuesMin, issuesMax, pullsMin, pullsMax, branchesMin, branchesMax,
                            releasesMin, releasesMax, starsMin, starsMax, watchersMin, watchersMax, forksMin, forksMax,
                            createdMin, createdMax, committedMin, committedMax, excludeForks, onlyForks, hasIssues,
                            hasPulls, hasWiki, hasLicense
                    )
            ).withRel("csv").expand().toString();
            String xml = ControllerLinkBuilder.linkTo(
                    ControllerLinkBuilder.methodOn(GitRepoController.class).downloadRepos(
                            "xml", name, nameEquals, language, license, label, commitsMin, commitsMax, contributorsMin,
                            contributorsMax, issuesMin, issuesMax, pullsMin, pullsMax, branchesMin, branchesMax,
                            releasesMin, releasesMax, starsMin, starsMax, watchersMin, watchersMax, forksMin, forksMax,
                            createdMin, createdMax, committedMin, committedMax, excludeForks, onlyForks, hasIssues,
                            hasPulls, hasWiki, hasLicense
                    )
            ).withRel("xml").expand().toString();
            String json = ControllerLinkBuilder.linkTo(
                    ControllerLinkBuilder.methodOn(GitRepoController.class).downloadRepos(
                            "json", name, nameEquals, language, license, label, commitsMin, commitsMax, contributorsMin,
                            contributorsMax, issuesMin, issuesMax, pullsMin, pullsMax, branchesMin, branchesMax,
                            releasesMin, releasesMax, starsMin, starsMax, watchersMin, watchersMax, forksMin, forksMax,
                            createdMin, createdMax, committedMin, committedMax, excludeForks, onlyForks, hasIssues,
                            hasPulls, hasWiki, hasLicense
                    )
            ).withRel("json").expand().toString();

            download.add(csv);
            download.add(xml);
            download.add(json);
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok();
        responseBuilder = responseBuilder.header("Links", String.join(", ", links));
        responseBuilder = responseBuilder.header("Download", String.join(", ", download));
        return responseBuilder.body(
                new LinkedHashMap<>(){{
                    put("totalPages", totalPages);
                    put("totalItems", totalItems);
                    put("page", page + 1);
                    put("items", dtos);
                }}
        );
    }

    @GetMapping(value = "/r/download/{format}")
    public ResponseEntity<?> downloadRepos(
            @PathVariable("format") String format,
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
        if(!format.matches("csv|json|xml"))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        Range<Long> commits = Ranges.build(commitsMin, commitsMax);
        Range<Long> contributors = Ranges.build(contributorsMin, contributorsMax);
        Range<Long> issues = Ranges.build(issuesMin, issuesMax);
        Range<Long> pulls = Ranges.build(pullsMin, pullsMax);
        Range<Long> branches = Ranges.build(branchesMin, branchesMax);
        Range<Long> releases = Ranges.build(releasesMin, releasesMax);
        Range<Long> stars = Ranges.build(starsMin, starsMax);
        Range<Long> watchers = Ranges.build(watchersMin, watchersMax);
        Range<Long> forks = Ranges.build(forksMin, forksMax);
        Range<Date> created = Ranges.build(createdMin, createdMax);
        Range<Date> committed = Ranges.build(committedMin, committedMax);

        List<GitRepo> results = gitRepoService.findDynamically(
                name, nameEquals, language, license, label, commits, contributors, issues, pulls, branches, releases, stars,
                watchers, forks, created, committed, excludeForks, onlyForks, hasIssues, hasPulls, hasWiki, hasLicense
        );

        List<GitRepoDto> dtos = List.of(conversionService.convert(results.toArray(new GitRepo[0]), GitRepoDto[].class));

        String tempFileName = System.currentTimeMillis() + ".temp";
        File tempFile = new File(downloadFolder + tempFileName);
        tempFile.getParentFile().mkdirs();

        String mediaType = "text/";
        try {
            switch (format){
                case "csv":
                    mediaType += format;
                    csvMapper.writerWithDefaultPrettyPrinter().with(csvSchema).writeValue(tempFile, dtos);
                    break;
                case "json":
                    mediaType += "plain";
                    objMapper.writerWithDefaultPrettyPrinter().writeValue(tempFile, dtos);
                    break;
                case "xml":
                    mediaType += format;
                    xmlMapper.writerWithDefaultPrettyPrinter().writeValue(tempFile, dtos);
                    break;
            }
        } catch (IOException ex) {
            log.error(ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=results." + format)
                .contentLength(tempFile.length())
                .contentType(MediaType.parseMediaType(mediaType))
                .body(new FileSystemResourceCustom(tempFile));
    }

    @GetMapping("/r/{repoId}")
    public ResponseEntity<?> getRepoById(@PathVariable(value = "repoId") Long repoId){
        Optional<GitRepo> optional = gitRepoService.getRepoById(repoId);
        return optional.map(gitRepo -> {
            GitRepoDto dto = conversionService.convert(optional.get(), GitRepoDto.class);
            return ResponseEntity.ok(dto);
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/r/labels")
    public ResponseEntity<?> getAllLabels(){
        return ResponseEntity.ok(Map.of("items", gitRepoService.getAllLabels()));
    }

    @GetMapping("/r/languages")
    public ResponseEntity<?> getAllLanguages(){
        return ResponseEntity.ok(Map.of("items", gitRepoService.getAllLanguages()));
    }

    @GetMapping("/r/licenses")
    public ResponseEntity<?> getAllLicenses() {
        return ResponseEntity.ok(Map.of("items", gitRepoService.getAllLicenses()));
    }
}
