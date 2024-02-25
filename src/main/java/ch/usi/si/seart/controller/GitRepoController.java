package ch.usi.si.seart.controller;

import ch.usi.si.seart.dto.GitRepoCsvDto;
import ch.usi.si.seart.dto.GitRepoDto;
import ch.usi.si.seart.dto.SearchParameterDto;
import ch.usi.si.seart.hateoas.LinkBuilder;
import ch.usi.si.seart.model.GitRepo;
import ch.usi.si.seart.model.GitRepo_;
import ch.usi.si.seart.model.Language;
import ch.usi.si.seart.model.License;
import ch.usi.si.seart.service.GitRepoService;
import ch.usi.si.seart.service.LanguageService;
import ch.usi.si.seart.service.LicenseService;
import ch.usi.si.seart.service.StatisticsService;
import ch.usi.si.seart.web.ExportFormat;
import ch.usi.si.seart.web.Headers;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpHeaders;
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

@SuppressWarnings("ConstantConditions")
@Slf4j
@RestController
@RequestMapping("/r")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(
        name = "git-repo",
        description = "Endpoints used for retrieving information regarding mined repositories."
)
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

    @Value("${spring.jpa.properties.hibernate.jdbc.fetch_size}")
    Long fetchSize;

    CsvSchema csvSchema;

    @Qualifier("csvMapper")
    CsvMapper csvMapper;
    @Qualifier("jsonMapper")
    JsonMapper jsonMapper;
    @Qualifier("xmlMapper")
    XmlMapper xmlMapper;

    GitRepoService gitRepoService;
    LicenseService licenseService;
    LanguageService languageService;
    ConversionService conversionService;
    StatisticsService statisticsService;

    @PersistenceContext
    EntityManager entityManager;

    LinkBuilder<Page<?>> searchLinkBuilder;
    LinkBuilder<Void> downloadLinkBuilder;

    @SuppressWarnings("unchecked")
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Perform a search for GitHub repositories matching a set of specified criteria.")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request body format or unsupported sorting property")
    public ResponseEntity<?> searchRepos(
            @Parameter(description = "The repository match criteria", in = ParameterIn.QUERY)
            SearchParameterDto searchParameterDto,
            @Parameter(description = "The search pagination settings", in = ParameterIn.QUERY)
            @SortDefault(sort = GitRepo_.NAME)
            Pageable pageable,
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

        Specification<GitRepo> specification = conversionService.convert(searchParameterDto, Specification.class);
        Page<GitRepo> results = gitRepoService.getBy(specification, pageRequest);

        List<GitRepoDto> dtos = List.of(
                conversionService.convert(
                        results.getContent().toArray(GitRepo[]::new), GitRepoDto[].class
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
        headers.add(Headers.X_LINK_SEARCH, searchLinkBuilder.getLinks(request, results));
        headers.add(Headers.X_LINK_DOWNLOAD, downloadLinkBuilder.getLinks(request));

        return new ResponseEntity<>(resultPage, headers, HttpStatus.OK);
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    @GetMapping(value = "/download/{format}")
    @Operation(summary = "Export GitHub repositories matching a set of specified criteria to a file format.")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request body format or unsupported export format")
    public void downloadRepos(
            @Parameter(description = "Export file format", in = ParameterIn.PATH, example = "csv")
            @PathVariable("format")
            ExportFormat format,
            @Parameter(description = "The repository match criteria", in = ParameterIn.QUERY)
            SearchParameterDto searchParameterDto,
            HttpServletResponse response
    ) throws IOException {
        response.setHeader(HttpHeaders.CONTENT_TYPE, "application/gzip");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=results." + format + ".gz");
        Specification<GitRepo> specification = conversionService.convert(searchParameterDto, Specification.class);
        JsonFactory factory = conversionService.convert(format, JsonFactory.class);
        GZIPOutputStream outputStream = new GZIPOutputStream(response.getOutputStream());
        @Cleanup JsonGenerator jsonGenerator = factory.createGenerator(outputStream);
        @Cleanup Stream<GitRepoDto> results = gitRepoService.streamBy(specification)
                .map(gitRepo -> {
                    GitRepoDto dto = conversionService.convert(gitRepo, GitRepoDto.class);
                    entityManager.detach(gitRepo);
                    return dto;
                });
        configure(jsonGenerator);
        beforeWriteResults(jsonGenerator, searchParameterDto);
        writeResults(jsonGenerator, results::iterator);
        afterWriteResults(jsonGenerator);
    }

    private void configure(JsonGenerator jsonGenerator) {
        if (jsonGenerator instanceof CsvGenerator csvGenerator) {
            csvGenerator.setCodec(csvMapper);
            csvGenerator.setSchema(csvSchema);
            csvGenerator.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
            csvGenerator.configure(CsvGenerator.Feature.ALWAYS_QUOTE_NUMBERS, true);
            csvGenerator.configure(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS, true);
            csvGenerator.configure(CsvGenerator.Feature.ALWAYS_QUOTE_EMPTY_STRINGS, true);
        } else if (jsonGenerator instanceof ToXmlGenerator xmlGenerator) {
            xmlGenerator.setCodec(xmlMapper);
            xmlGenerator.configure(ToXmlGenerator.Feature.WRITE_XML_1_1, true);
            xmlGenerator.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        } else {
            jsonGenerator.setCodec(jsonMapper);
        }
    }

    private void beforeWriteResults(
            JsonGenerator jsonGenerator, SearchParameterDto searchParameterDto
    ) throws IOException {
        if (jsonGenerator instanceof CsvGenerator) return;
        if (jsonGenerator instanceof ToXmlGenerator xmlGenerator) {
            xmlGenerator.initGenerator();
            xmlGenerator.setNextName(new QName("results"));
            xmlGenerator.writeStartObject();
            String attributes = searchParameterDto.toMap().entrySet().stream()
                    .map(entry -> String.format("%s=\"%s\"", entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining(" "));
            String parameters = String.format("<parameters %s/>", attributes);
            xmlGenerator.writeRaw(parameters);
        } else {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeObjectField("parameters", searchParameterDto.toMap());
        }
    }

    private void writeResults(JsonGenerator jsonGenerator, Iterable<GitRepoDto> dtos) throws IOException {
        PersistenceContextInvalidator invalidator = new PersistenceContextInvalidator();
        if (jsonGenerator instanceof CsvGenerator csvGenerator) {
            for (GitRepoDto dto : dtos) {
                GitRepoCsvDto csvDto = conversionService.convert(dto, GitRepoCsvDto.class);
                csvGenerator.writePOJO(csvDto);
                invalidator.increment();
            }
        } else if (jsonGenerator instanceof ToXmlGenerator xmlGenerator) {
            xmlGenerator.writeFieldName("items");
            xmlGenerator.writeStartObject();
            for (GitRepoDto dto : dtos) {
                xmlGenerator.writePOJOField("item", dto);
                invalidator.increment();
            }
            xmlGenerator.writeEndObject();
        } else {
            jsonGenerator.writeArrayFieldStart("items");
            for (GitRepoDto dto : dtos) {
                jsonGenerator.writePOJO(dto);
                invalidator.increment();
            }
            jsonGenerator.writeEndArray();
        }
    }

    private final class PersistenceContextInvalidator {

        AtomicLong counter = new AtomicLong(0);

        void increment() {
            long value = counter.incrementAndGet();
            boolean condition = value % fetchSize == 0;
            if (condition) entityManager.clear();
        }
    }

    private void afterWriteResults(JsonGenerator jsonGenerator) throws IOException {
        if (jsonGenerator instanceof CsvGenerator) return;
        jsonGenerator.writeEndObject();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Retrieve repository information based on its internal identifier (ID).")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad repository ID format")
    @ApiResponse(responseCode = "404", description = "Repository with requested ID does not exist")
    public ResponseEntity<?> getRepoById(
            @Parameter(description = "The repository identifier", in = ParameterIn.PATH, example = "0")
            @PathVariable(value = "id")
            Long id
    ) {
        GitRepo gitRepo = gitRepoService.getById(id);
        GitRepoDto dto = conversionService.convert(gitRepo, GitRepoDto.class);
        return ResponseEntity.ok(dto);
    }

    @GetMapping(value = "/{owner}/{repo}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Retrieve repository information based on its full name.")
    public ResponseEntity<?> getRepoByName(
            @PathVariable(value = "owner") String owner,
            @PathVariable(value = "repo") String repo
    ) {
        GitRepo gitRepo = gitRepoService.getByName(owner + "/" + repo);
        GitRepoDto dto = conversionService.convert(gitRepo, GitRepoDto.class);
        return ResponseEntity.ok(dto);
    }

    @GetMapping(value = "/labels", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Retrieve a list of the 500 most popular issue labels mined across projects.")
    public ResponseEntity<?> getAllLabels() {
        return ResponseEntity.ok(statisticsService.getTopRankedLabelNames());
    }

    @GetMapping(value = "/languages", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Retrieve a list of all repository languages mined across projects.")
    public ResponseEntity<?> getAllLanguages() {
        return ResponseEntity.ok(
                languageService.getRanked().stream()
                        .map(Language::getName)
                        .toList()
        );
    }

    @GetMapping(value = "/licenses", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Retrieve a list of all repository licenses mined across projects.")
    public ResponseEntity<?> getAllLicenses() {
        return ResponseEntity.ok(
                licenseService.getRanked().stream()
                        .map(License::getName)
                        .toList()
        );
    }

    @GetMapping(value = "/topics", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Retrieve a list of all repository topics mined across projects.")
    public ResponseEntity<?> getAllTopics() {
        return ResponseEntity.ok(statisticsService.getTopRankedTopicNames());
    }

    @GetMapping("/stats")
    @Operation(summary = "Retrieve the number of repositories mined and analyzed for each supported language.")
    public ResponseEntity<?> getRepoStatistics() {
        return ResponseEntity.ok(statisticsService.getMainLanguageStats());
    }
}
