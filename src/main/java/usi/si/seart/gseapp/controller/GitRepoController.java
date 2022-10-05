package usi.si.seart.gseapp.controller;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import usi.si.seart.gseapp.db_access_service.GitRepoService;
import usi.si.seart.gseapp.dto.GitRepoDto;
import usi.si.seart.gseapp.io.SelfDestructingResource;
import usi.si.seart.gseapp.jackson.JsonWrapper;
import usi.si.seart.gseapp.jackson.XmlWrapper;
import usi.si.seart.gseapp.model.GitRepo;
import usi.si.seart.gseapp.model.GitRepo_;
import usi.si.seart.gseapp.util.Ranges;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GitRepoController {

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
            GitRepo_.LAST_COMMIT
    );

    @NonFinal
    @Value(value = "${app.search.page-size}")
    Integer pageSize;

    @Qualifier("exportFolder")
    String exportFolder;

    @Qualifier("exportFormats")
    Set<String> exportFormats;

    CsvSchema csvSchema;

    @Qualifier("csvMapper")
    CsvMapper csvMapper;
    @Qualifier("jsonMapper")
    JsonMapper jsonMapper;
    @Qualifier("xmlMapper")
    XmlMapper xmlMapper;

    GitRepoService gitRepoService;
    ConversionService conversionService;

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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date createdMin,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date createdMax,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date committedMin,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date committedMax,
            @RequestParam(required = false, defaultValue = "false") Boolean excludeForks,
            @RequestParam(required = false, defaultValue = "false") Boolean onlyForks,
            @RequestParam(required = false, defaultValue = "false") Boolean hasIssues,
            @RequestParam(required = false, defaultValue = "false") Boolean hasPulls,
            @RequestParam(required = false, defaultValue = "false") Boolean hasWiki,
            @RequestParam(required = false, defaultValue = "false") Boolean hasLicense,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = GitRepo_.NAME) String sort,
            HttpServletRequest request
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

        UriComponentsBuilder searchBuilder = ServletUriComponentsBuilder.fromServletMapping(request).path("/r/search");
        UriComponentsBuilder downloadBuilder = ServletUriComponentsBuilder.fromServletMapping(request).path("/r/download");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>(
                request.getParameterMap()
                        .entrySet()
                        .stream()
                        .map(entry -> {
                            List<String> regular = Lists.newArrayList(entry.getValue());
                            List<String> encoded = Lists.transform(regular, value -> URLEncoder.encode(value, StandardCharsets.UTF_8));
                            return Map.entry(entry.getKey(), encoded);
                        })
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue
                        ))
        );

        searchBuilder.queryParams(params);
        links.add(Link.of(searchBuilder.build().toString(), IanaLinkRelations.SELF).toString());

        if (!results.isFirst()){
            searchBuilder.replaceQueryParam("page", 0);
            links.add(Link.of(searchBuilder.build().toString(), IanaLinkRelations.FIRST).toString());
        }

        if (results.hasPrevious()){
            searchBuilder.replaceQueryParam("page", page - 1);
            links.add(Link.of(searchBuilder.build().toString(), IanaLinkRelations.PREV).toString());
        }

        if (results.hasNext()){
            searchBuilder.replaceQueryParam("page", page + 1);
            links.add(Link.of(searchBuilder.build().toString(), IanaLinkRelations.NEXT).toString());
        }

        if (!results.isLast()){
            searchBuilder.replaceQueryParam("page", totalPages - 1);
            links.add(Link.of(searchBuilder.build().toString(), IanaLinkRelations.LAST).toString());
        }

        params.remove("page");
        params.remove("sort");
        searchBuilder.replaceQueryParams(params);
        links.add(Link.of(searchBuilder.build().toString(), "base").toString());

        List<String> download = new ArrayList<>();

        if (totalItems > 0) {
            downloadBuilder.queryParams(params);
            download.addAll(
                    List.of(
                            Link.of(downloadBuilder.cloneBuilder().pathSegment("csv").build().toString(), "csv").toString(),
                            Link.of(downloadBuilder.cloneBuilder().pathSegment("xml").build().toString(), "xml").toString(),
                            Link.of(downloadBuilder.cloneBuilder().pathSegment("json").build().toString(), "json").toString()
                    )
            );
        }

        Map<String, Object> resultPage = new LinkedHashMap<>();
        resultPage.put("totalPages", totalPages);
        resultPage.put("totalItems", totalItems);
        resultPage.put("page", page + 1);
        resultPage.put("items", dtos);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Links", String.join(", ", links));
        headers.add("Download", String.join(", ", download));

        return new ResponseEntity<>(resultPage, headers, HttpStatus.OK);
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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date createdMin,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date createdMax,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date committedMin,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date committedMax,
            @RequestParam(required = false, defaultValue = "false") Boolean excludeForks,
            @RequestParam(required = false, defaultValue = "false") Boolean onlyForks,
            @RequestParam(required = false, defaultValue = "false") Boolean hasIssues,
            @RequestParam(required = false, defaultValue = "false") Boolean hasPulls,
            @RequestParam(required = false, defaultValue = "false") Boolean hasWiki,
            @RequestParam(required = false, defaultValue = "false") Boolean hasLicense
    ){
        if(!exportFormats.contains(format))
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
        File tempFile = new File(exportFolder, tempFileName);

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
                    csvMapper.writer().with(csvSchema).writeValue(tempFile, dtos);
                    break;
                case "json":
                    mediaType += "plain";
                    jsonMapper.writer().writeValue(tempFile, new JsonWrapper(searchParams, dtos.size(), dtos));
                    break;
                case "xml":
                    mediaType += format;
                    xmlMapper.writer().withRootName("result").writeValue(tempFile, new XmlWrapper(searchParams, dtos));
                    break;
                default:
                    throw new IllegalStateException("Default portion of this switch should not be reachable!");
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
        return ResponseEntity.ok(gitRepoService.getAllLabels(500));
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
