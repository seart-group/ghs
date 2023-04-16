package usi.si.seart.controller;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import usi.si.seart.dto.GitRepoDto;
import usi.si.seart.dto.SearchParameterDto;
import usi.si.seart.hateoas.DownloadLinkBuilder;
import usi.si.seart.hateoas.SearchLinkBuilder;
import usi.si.seart.model.GitRepo;
import usi.si.seart.model.GitRepo_;
import usi.si.seart.service.GitRepoService;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    SearchLinkBuilder searchLinkBuilder;
    DownloadLinkBuilder downloadLinkBuilder;

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
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

        Map<String, Object> resultPage = new LinkedHashMap<>();
        resultPage.put("totalPages", totalPages);
        resultPage.put("totalItems", totalItems);
        resultPage.put("page", page + 1);
        resultPage.put("items", dtos);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("X-Link-Search", searchLinkBuilder.getLinks(request, results));
        headers.add("X-Link-Download", downloadLinkBuilder.getLinks(request));

        return new ResponseEntity<>(resultPage, headers, HttpStatus.OK);
    }

    @SneakyThrows({ IOException.class })
    @Transactional(readOnly = true)
    @GetMapping(value = "/download/{format}")
    public void downloadRepos(
            @PathVariable("format") String format,
            SearchParameterDto searchParameterDto,
            HttpServletResponse response
    ) {
        if (!exportFormats.contains(format)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String contentType;
        switch (format) {
            case "json":
                contentType = "application/" + format;
                break;
            case "csv":
            case "xml":
                contentType = "text/" + format;
                break;
            default:
                throw new IllegalStateException("Default portion of this switch should not be reachable!");
        }

        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "attachment;filename=results." + format);

        PrintWriter writer = response.getWriter();
        JsonGenerator generator;
        switch (format) {
            case "csv":
                generator = new CsvFactory().createGenerator(writer);
                break;
            case "json":
                generator = new JsonFactory().createGenerator(writer);
                break;
            case "xml":
                generator = new XmlFactory().createGenerator(writer);
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

        switch (format) {
            case "csv":
                generator.setCodec(csvMapper);
                generator.setSchema(csvSchema);
                writeResults((CsvGenerator) generator, dtos, searchParameterDto);
                break;
            case "json":
                generator.setCodec(jsonMapper);
                writeResults(generator, dtos, searchParameterDto);
                break;
            case "xml":
                generator.setCodec(xmlMapper);
                writeResults((ToXmlGenerator) generator, dtos, searchParameterDto);
                break;
            default:
                throw new IllegalStateException("Default portion of this switch should not be reachable!");
        }

        generator.close();
    }

    private void writeResults(
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

    private void writeResults(
            ToXmlGenerator generator, Iterable<GitRepoDto> dtos, SearchParameterDto searchParameterDto
    ) throws IOException {
        generator.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        generator.configure(ToXmlGenerator.Feature.WRITE_XML_1_1, true);
        generator.initGenerator();
        generator.setNextName(new QName("results"));
        generator.writeStartObject();

        String attributes = searchParameterDto.toMap().entrySet().stream()
                .map(entry -> String.format("%s=\"%s\"", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(" "));
        String parameters = String.format("<parameters %s/>", attributes);
        generator.writeRaw(parameters);

        generator.writeFieldName("items");
        generator.writeStartObject();
        for (GitRepoDto dto : dtos) {
            generator.writePOJOField("item", dto);
        }
        generator.writeEndObject();

        generator.writeEndObject();
    }

    private void writeResults(
            CsvGenerator generator, Iterable<GitRepoDto> dtos, SearchParameterDto searchParameterDto
    ) throws IOException {
        generator.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        generator.configure(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS, true);
        for (GitRepoDto dto : dtos) {
            generator.writePOJO(dto);
        }
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getRepoById(@PathVariable(value = "id") Long id){
        GitRepo gitRepo = gitRepoService.getRepoById(id);
        GitRepoDto dto = conversionService.convert(gitRepo, GitRepoDto.class);
        return ResponseEntity.ok(dto);
    }

    @GetMapping(value = "/labels", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllLabels(){
        return ResponseEntity.ok(gitRepoService.getAllLabels(500));
    }

    @GetMapping(value = "/languages", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllLanguages(){
        return ResponseEntity.ok(gitRepoService.getAllLanguages());
    }

    @GetMapping(value = "/licenses", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllLicenses() {
        return ResponseEntity.ok(gitRepoService.getAllLicenses());
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getRepoStatistics(){
        return ResponseEntity.ok(gitRepoService.getMainLanguageStatistics());
    }
}
