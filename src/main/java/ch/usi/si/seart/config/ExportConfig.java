package ch.usi.si.seart.config;

import ch.usi.si.seart.dto.GitRepoDto;
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
import java.util.List;
import java.util.Set;

@Configuration
public class ExportConfig {

    @Bean
    public Set<String> exportFormats() {
        return Set.of("csv", "json", "xml");
    }

    @Bean
    DateFormat exportTimeFormat() {
        return new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper(DateFormat exportTimeFormat) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setDateFormat(exportTimeFormat);
        return objectMapper;
    }

    @Bean
    public JsonMapper jsonMapper(DateFormat exportTimeFormat) {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .defaultDateFormat(exportTimeFormat)
                .build();
    }

    @Bean
    public CsvMapper csvMapper(DateFormat exportTimeFormat) {
        return CsvMapper.builder()
                .addModule(new JavaTimeModule())
                .defaultDateFormat(exportTimeFormat)
                .enable(JsonGenerator.Feature.IGNORE_UNKNOWN)
                .enable(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS)
                .build();
    }

    @Bean
    public XmlMapper xmlMapper(DateFormat exportTimeFormat) {
        return XmlMapper.builder()
                .addModule(new JavaTimeModule())
                .defaultDateFormat(exportTimeFormat)
                .enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION)
                .enable(ToXmlGenerator.Feature.WRITE_XML_1_1)
                .build();
    }

    @Bean
    public CsvSchema csvSchema() {
        List<String> fields = Arrays.stream(GitRepoDto.class.getDeclaredFields())
                .map(Field::getName)
                .toList();

        CsvSchema.Builder schemaBuilder = CsvSchema.builder();
        for (String field : fields) {
            schemaBuilder.addColumn(field);
        }

        // TODO: Add option for CSV comments
        return schemaBuilder.build().withHeader();
    }
}
