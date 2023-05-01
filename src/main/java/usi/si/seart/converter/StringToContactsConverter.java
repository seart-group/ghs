package usi.si.seart.converter;

import io.swagger.v3.oas.models.info.Contact;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringToContactsConverter implements Converter<String, Contact[]> {

    Pattern pattern = Pattern.compile("Contributor\\s\\{name=([^,]+),\\semail=([^\\}]+)}");

    @Override
    @NonNull
    public Contact[] convert(@NonNull String source) {
        Matcher matcher = pattern.matcher(source);
        List<Contact> contacts = new ArrayList<>();
        while (matcher.find()) {
            Contact contact = new Contact()
                    .name(matcher.group(1))
                    .email(matcher.group(2));
            contacts.add(contact);
        }
        return contacts.toArray(new Contact[0]);
    }
}
