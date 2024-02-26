package ch.usi.si.seart.converter;

import ch.usi.si.seart.web.ExportFormat;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExportFormatToJsonFactoryConverter implements Converter<ExportFormat, JsonFactory> {

    @Qualifier("csvMapper")
    CsvMapper csvMapper;
    @Qualifier("jsonMapper")
    JsonMapper jsonMapper;
    @Qualifier("xmlMapper")
    XmlMapper xmlMapper;

    @Override
    @NonNull
    public JsonFactory convert(@NonNull ExportFormat source) {
        return switch (source) {
            case CSV -> csvMapper.getFactory().copy();
            case JSON -> jsonMapper.getFactory().copy();
            case XML -> xmlMapper.getFactory().copy();
        };
    }
}
