package usi.si.seart.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import usi.si.seart.dto.GitRepoDto;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Configuration
public class ExportConfig {

    @Bean
    public String exportFolder() {
        return "export_tmp";
    }

    @Bean
    public Set<String> exportFormats() {
        return Set.of("csv", "json", "xml");
    }

    @Bean
    public DateFormat exportTimeFormat() {
        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(exportTimeFormat());
        return objectMapper;
    }

    @Bean
    public JsonMapper jsonMapper() {
        JsonMapper jsonMapper = new JsonMapper();
        jsonMapper.setDateFormat(exportTimeFormat());
        return jsonMapper;
    }

    @Bean
    public CsvMapper csvMapper() {
        CsvMapper csvMapper = new CsvMapper();
        csvMapper.setDateFormat(exportTimeFormat());
        csvMapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        csvMapper.configure(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS, true);
        return csvMapper;
    }

    @Bean
    public XmlMapper xmlMapper() {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_1_1, true);
        xmlMapper.setDateFormat(exportTimeFormat());
        return xmlMapper;
    }

    @Bean
    public CsvSchema csvSchema() {
        Set<String> exclusions = Set.of("id", "labels", "languages");

        List<String> fields = Arrays.stream(GitRepoDto.class.getDeclaredFields())
                .map(Field::getName)
                .filter(Predicate.not(exclusions::contains))
                .collect(Collectors.toList());

        CsvSchema.Builder schemaBuilder = CsvSchema.builder();
        for (String field : fields){
            schemaBuilder.addColumn(field);
        }

        // TODO: Add option for CSV comments
        return schemaBuilder.build().withHeader();
    }
}
