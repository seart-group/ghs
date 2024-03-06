package ch.usi.si.seart.converter;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;

@Component
public class StringToURLConverter implements Converter<String, URL> {

    @Override
    @NotNull
    public URL convert(@NotNull String source) {
        try {
            return new URL(source);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
