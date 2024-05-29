package ch.usi.si.seart.config;

import ch.usi.si.seart.dto.GitRepoDto;
import ch.usi.si.seart.jackson.PaginationModule;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;

@Configuration
public class JacksonConfig {

    @Bean
    DateFormat exportTimeFormat() {
        return new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper(DateFormat exportTimeFormat) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new PaginationModule());
        objectMapper.setDateFormat(exportTimeFormat);
        return objectMapper;
    }

    @Bean
    public JsonMapper jsonMapper(DateFormat exportTimeFormat) {
        return JsonMapper.builder()
                .addModules(
                        new JavaTimeModule(),
                        new PaginationModule()
                )
                .defaultDateFormat(exportTimeFormat)
                .build();
    }

    @Bean
    public CsvMapper csvMapper(DateFormat exportTimeFormat) {
        return CsvMapper.builder()
                .addModules(
                        new JavaTimeModule(),
                        new PaginationModule()
                )
                .defaultDateFormat(exportTimeFormat)
                .enable(JsonGenerator.Feature.IGNORE_UNKNOWN)
                .enable(CsvGenerator.Feature.ALWAYS_QUOTE_NUMBERS)
                .enable(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS)
                .enable(CsvGenerator.Feature.ALWAYS_QUOTE_EMPTY_STRINGS)
                .build();
    }

    @Bean
    public XmlMapper xmlMapper(DateFormat exportTimeFormat) {
        return XmlMapper.builder()
                .addModules(
                        new JavaTimeModule(),
                        new PaginationModule()
                )
                .defaultDateFormat(exportTimeFormat)
                .enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION)
                .enable(ToXmlGenerator.Feature.WRITE_XML_1_1)
                .build();
    }

    @Bean
    public CsvSchema csvSchema() {
        return Arrays.stream(GitRepoDto.class.getDeclaredFields())
                .map(Field::getName)
                .reduce(
                        CsvSchema.builder(),
                        CsvSchema.Builder::addColumn,
                        (first, second) -> first.addColumns(second::getColumns)
                )
                .build()
                .withHeader();
    }
}
