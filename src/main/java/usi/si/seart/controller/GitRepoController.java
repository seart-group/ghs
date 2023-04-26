package usi.si.seart.controller;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.TokenStreamFactory;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.csv.CsvFactory;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import usi.si.seart.dto.GitRepoCsvDto;
import usi.si.seart.dto.GitRepoDto;
import usi.si.seart.dto.SearchParameterDto;
import usi.si.seart.model.GitRepo;
import usi.si.seart.model.GitRepo_;
import usi.si.seart.service.GitRepoService;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.PrintWriter;
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
@RequestMapping("/r")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GitRepoController {

    private static final Set<String> supportedFields = Set.of(
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

    EntityManager entityManager;

    @GetMapping("/search")
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

    @Transactional(readOnly = true)
    @GetMapping("/download/{format}")
    @SneakyThrows({ IOException.class })
    public void downloadRepos(
            @PathVariable("format") String format,
            SearchParameterDto searchParameterDto,
            HttpServletResponse response
    ) {
        if (!exportFormats.contains(format)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        response.setContentType("text/" + format);
        response.setHeader("Content-Disposition", "attachment;filename=results." + format);

        @Cleanup PrintWriter printWriter = response.getWriter();
        TokenStreamFactory factory;
        JsonGenerator generator;

        switch (format) {
            case "csv":
                factory = new CsvFactory();
                break;
            case "json":
                factory = new JsonFactory();
                break;
            case "xml":
                factory = new XmlFactory();
                break;
            default:
                throw new IllegalStateException("Default portion of this switch should not be reachable!");
        }

        Map<String, Object> paramMap = searchParameterDto.toParameterMap();
        @Cleanup Stream<GitRepoDto> results = gitRepoService.streamDynamically(paramMap)
                .map(gitRepo -> {
                    GitRepoDto dto = conversionService.convert(gitRepo, GitRepoDto.class);
                    entityManager.detach(gitRepo);
                    return dto;
                });
        Iterable<GitRepoDto> dtos = results::iterator;

        generator = factory.createGenerator(printWriter);

        switch (format) {
            case "csv":
                generator.setCodec(csvMapper);
                generator.setSchema(csvSchema);
                writeCsv(generator, dtos, searchParameterDto);
                break;
            case "json":
                generator.setCodec(jsonMapper);
                writeJson(generator, dtos, searchParameterDto);
                break;
            case "xml":
                generator.setCodec(xmlMapper);
                writeXml(generator, dtos, searchParameterDto);
                break;
            default:
                throw new IllegalStateException("Default portion of this switch should not be reachable!");
        }

        generator.close();
    }

    private void writeJson(
            JsonGenerator generator, Iterable<GitRepoDto> dtos, SearchParameterDto searchParameterDto
    ) throws IOException {
        generator.writeStartObject();
        generator.writeObjectField("parameters", searchParameterDto);
        generator.writeArrayFieldStart("items");
        for (GitRepoDto dto : dtos) {
            generator.writePOJO(dto);
        }
        generator.writeEndArray();
        generator.writeEndObject();
    }

    private void writeXml(
            JsonGenerator generator, Iterable<GitRepoDto> dtos, SearchParameterDto searchParameterDto
    ) throws IOException {
        ToXmlGenerator xmlGenerator = (ToXmlGenerator) generator;
        xmlGenerator.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        xmlGenerator.configure(ToXmlGenerator.Feature.WRITE_XML_1_1, true);
        xmlGenerator.initGenerator();
        xmlGenerator.setNextName(new QName("results"));
        xmlGenerator.writeStartObject();

        String attributes = searchParameterDto.toMap().entrySet().stream()
                .map(entry -> String.format("%s=\"%s\"", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(" "));
        String parameters = String.format("<parameters %s/>", attributes);
        xmlGenerator.writeRaw(parameters);

        xmlGenerator.writeFieldName("items");
        xmlGenerator.writeStartObject();
        for (GitRepoDto dto : dtos) {
            xmlGenerator.writePOJOField("item", dto);
        }
        xmlGenerator.writeEndObject();

        xmlGenerator.writeEndObject();
    }

    private void writeCsv(
            JsonGenerator generator, Iterable<GitRepoDto> dtos, SearchParameterDto searchParameterDto
    ) throws IOException {
        CsvGenerator csvGenerator = (CsvGenerator) generator;
        csvGenerator.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        csvGenerator.configure(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS, true);
        for (GitRepoDto dto : dtos) {
            GitRepoCsvDto csvDto = conversionService.convert(dto, GitRepoCsvDto.class);
            generator.writePOJO(csvDto);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRepoById(@PathVariable(value = "id") Long id){
        Optional<GitRepo> optional = gitRepoService.getRepoById(id);
        return optional.map(gitRepo -> {
            GitRepoDto dto = conversionService.convert(optional.get(), GitRepoDto.class);
            return ResponseEntity.ok(dto);
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/labels")
    public ResponseEntity<?> getAllLabels(){
        return ResponseEntity.ok(gitRepoService.getAllLabels(500));
    }

    @GetMapping("/languages")
    public ResponseEntity<?> getAllLanguages(){
        return ResponseEntity.ok(gitRepoService.getAllLanguages());
    }

    @GetMapping("/licenses")
    public ResponseEntity<?> getAllLicenses() {
        return ResponseEntity.ok(gitRepoService.getAllLicenses());
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getRepoStatistics(){
        return ResponseEntity.ok(gitRepoService.getMainLanguageStatistics());
    }
}
