package usi.si.seart.gseapp.controller;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import usi.si.seart.gseapp.db_access_service.GitRepoService;
import usi.si.seart.gseapp.dto.GitRepoDto;
import usi.si.seart.gseapp.dto.SearchParameterDto;
import usi.si.seart.gseapp.io.SelfDestructingResource;
import usi.si.seart.gseapp.jackson.JsonWrapper;
import usi.si.seart.gseapp.jackson.XmlWrapper;
import usi.si.seart.gseapp.model.GitRepo;
import usi.si.seart.gseapp.model.GitRepo_;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            SearchParameterDto searchParameterDto,
            @SortDefault(sort = GitRepo_.NAME) Pageable pageable,
            HttpServletRequest request
    ) {
        Set<String> sortFields = pageable.getSort().stream()
                .map(Sort.Order::getProperty)
                .collect(Collectors.toSet());
        if (!supportedFields.containsAll(sortFields))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        Sort sort = pageable.getSort();
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Map<String, Object> paramMap = searchParameterDto.toParameterMap();
        Page<GitRepo> results = gitRepoService.findDynamically(paramMap, pageRequest);

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
                            List<String> encoded = Stream.of(entry.getValue())
                                    .map(value -> URLEncoder.encode(value, StandardCharsets.UTF_8))
                                    .collect(Collectors.toList());
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
            SearchParameterDto searchParameterDto
    ){
        if (!exportFormats.contains(format))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        Map<String, Object> paramMap = searchParameterDto.toParameterMap();
        List<GitRepo> results = gitRepoService.findDynamically(paramMap);

        List<GitRepoDto> dtos = List.of(conversionService.convert(results.toArray(new GitRepo[0]), GitRepoDto[].class));

        String tempFileName = System.currentTimeMillis() + ".temp";
        File tempFile = new File(exportFolder, tempFileName);

        Map<String, Object> searchParams = searchParameterDto.toMap().entrySet().stream()
                .filter(entry -> ObjectUtils.isNotEmpty(entry.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (x, y) -> y,
                        LinkedHashMap::new
                ));

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
