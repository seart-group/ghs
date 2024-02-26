package ch.usi.si.seart.converter;

import ch.usi.si.seart.web.ExportFormat;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

public class StringToExportFormatConverter implements Converter<String, ExportFormat> {

    @Override
    @NonNull
    public ExportFormat convert(@NonNull String source) {
        return ExportFormat.valueOf(source.toUpperCase());
    }
}
