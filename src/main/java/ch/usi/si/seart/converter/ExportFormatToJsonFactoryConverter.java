package ch.usi.si.seart.converter;

import ch.usi.si.seart.web.ExportFormat;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.dataformat.csv.CsvFactory;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

public class ExportFormatToJsonFactoryConverter implements Converter<ExportFormat, JsonFactory> {

    @Override
    @NonNull
    public JsonFactory convert(@NonNull ExportFormat source) {
        return switch (source) {
            case CSV -> new CsvFactory();
            case JSON -> new JsonFactory();
            case XML -> new XmlFactory();
        };
    }
}
