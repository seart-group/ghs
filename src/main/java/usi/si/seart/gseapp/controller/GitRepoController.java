package usi.si.seart.gseapp.controller;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.google.common.collect.Range;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import usi.si.seart.gseapp.io.SelfDestructingResource;
import usi.si.seart.gseapp.jackson.JsonWrapper;
import usi.si.seart.gseapp.jackson.XmlWrapper;
import usi.si.seart.gseapp.model.GitRepo;
import usi.si.seart.gseapp.model.GitRepo_;
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
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("ConstantConditions")
@Slf4j
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GitRepoController {
    public static String downloadFolder = "download-temp";

    static CsvSchema csvSchema = GitRepoDto.getCsvSchema();
    static CsvMapper csvMapper;
    static ObjectMapper objMapper;
    static XmlMapper xmlMapper;

    static Set<String> supportedFields = Set.of(
            GitRepo_.NAME,
            GitRepo_.COMMITS,
            GitRepo_.CONTRIBUTORS,
            GitRepo_.TOTAL_ISSUES,
            GitRepo_.TOTAL_PULL_REQUESTS,
            GitRepo_.BRANCHES,
            GitRepo_.RELEASES,
            GitRepo_.STARGAZERS,
            GitRepo_.WATCHERS,
            GitRepo_.FORKS,
            GitRepo_.CREATED_AT,
            GitRepo_.PUSHED_AT
    );

    static Set<String> supportedFormats = Set.of("csv", "json", "xml");

    static {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        csvMapper = new CsvMapper();
        csvMapper.setDateFormat(df);
        csvMapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        objMapper = new ObjectMapper();
        objMapper.setDateFormat(df);
        xmlMapper = new XmlMapper();
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
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
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = GitRepo_.NAME) String sort
    ){
        if (!supportedFields.contains(sort))
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

        Integer pageSize = applicationPropertyService.getPageSize();
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by(sort).ascending());

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
                            hasPulls, hasWiki, hasLicense, 0, sort
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
                            hasPulls, hasWiki, hasLicense, page - 1, sort
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
                            hasPulls, hasWiki, hasLicense, page + 1, sort
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
                            hasPulls, hasWiki, hasLicense, totalPages - 1, sort
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
                        hasPulls, hasWiki, hasLicense, null, sort
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
        if(!supportedFormats.contains(format))
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
        File tempFile = new File(downloadFolder + "/" + tempFileName);

        Map<String, Object> searchParams = constructParameterMap(
                name, nameEquals, language, license, label, commitsMin, commitsMax, contributorsMin,
                contributorsMax, issuesMin, issuesMax, pullsMin, pullsMax, branchesMin, branchesMax,
                releasesMin, releasesMax, starsMin, starsMax, watchersMin, watchersMax, forksMin, forksMax,
                createdMin, createdMax, committedMin, committedMax, excludeForks, onlyForks, hasIssues,
                hasPulls, hasWiki, hasLicense
        );

        String mediaType = "text/";
        try {
            switch (format){
                case "csv":
                    mediaType += format;
                    csvMapper.writerWithDefaultPrettyPrinter()
                            .with(csvSchema)
                            .writeValue(tempFile, dtos);
                    break;
                case "json":
                    mediaType += "plain";
                    objMapper.writerWithDefaultPrettyPrinter()
                            .writeValue(tempFile, new JsonWrapper(searchParams, dtos.size(), dtos));
                    break;
                case "xml":
                    mediaType += format;
                    xmlMapper.writerWithDefaultPrettyPrinter()
                            .withRootName("result")
                            .writeValue(tempFile, new XmlWrapper(searchParams, dtos));
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
                .body(new SelfDestructingResource(tempFile));
    }

    private Map<String, Object> constructParameterMap(
            String name, Boolean nameEquals, String language, String license, String label, Long commitsMin, Long commitsMax,
            Long contributorsMin, Long contributorsMax, Long issuesMin, Long issuesMax, Long pullsMin, Long pullsMax,
            Long branchesMin, Long branchesMax, Long releasesMin, Long releasesMax, Long starsMin, Long starsMax,
            Long watchersMin, Long watchersMax, Long forksMin, Long forksMax, Date createdMin, Date createdMax,
            Date committedMin, Date committedMax, Boolean excludeForks, Boolean onlyForks, Boolean hasIssues,
            Boolean hasPulls, Boolean hasWiki, Boolean hasLicense
    ){
        Map<String, Object> map = new LinkedHashMap<>();
        if (StringUtils.isNotBlank(name)) map.put("name", name);
        map.put("nameEquals", nameEquals);
        if (StringUtils.isNotBlank(language)) map.put("language", language);
        if (StringUtils.isNotBlank(license)) map.put("license", license);
        if (StringUtils.isNotBlank(label)) map.put("label", label);
        map.put("commitsMin", commitsMin);
        map.put("commitsMax", commitsMax);
        map.put("contributorsMin", contributorsMin);
        map.put("contributorsMax", contributorsMax);
        map.put("issuesMin", issuesMin);
        map.put("issuesMax", issuesMax);
        map.put("pullsMin", pullsMin);
        map.put("pullsMax", pullsMax);
        map.put("branchesMin", branchesMin);
        map.put("branchesMax", branchesMax);
        map.put("releasesMin", releasesMin);
        map.put("releasesMax", releasesMax);
        map.put("starsMin", starsMin);
        map.put("starsMax", starsMax);
        map.put("watchersMin", watchersMin);
        map.put("watchersMax", watchersMax);
        map.put("forksMin", forksMin);
        map.put("forksMax", forksMax);
        map.put("createdMin", createdMin);
        map.put("createdMax", createdMax);
        map.put("committedMin", committedMin);
        map.put("committedMax", committedMax);
        map.put("excludeForks", excludeForks);
        map.put("onlyForks", onlyForks);
        map.put("hasIssues", hasIssues);
        map.put("hasPulls", hasPulls);
        map.put("hasWiki", hasWiki);
        map.put("hasLicense", hasLicense);

        return map.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (x, y) -> y,
                        LinkedHashMap::new
                ));
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
        return ResponseEntity.ok(Map.of("items", gitRepoService.getAllLabels(500)));
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
