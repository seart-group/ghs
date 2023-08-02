package usi.si.seart.converter;

import io.swagger.v3.oas.models.info.License;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringToLicensesConverter implements Converter<String, License[]> {

    private static final Pattern PATTERN = Pattern.compile("License\\s\\{name=([^,]+),\\surl=([^\\}]+)}");

    @Override
    @NonNull
    public License[] convert(@NonNull String source) {
        Matcher matcher = PATTERN.matcher(source);
        List<License> licenses = new ArrayList<>();
        while (matcher.find()) {
            License license = new License()
                    .name(matcher.group(1))
                    .url(matcher.group(2));
            licenses.add(license);
        }
        return licenses.toArray(new License[0]);
    }
}
