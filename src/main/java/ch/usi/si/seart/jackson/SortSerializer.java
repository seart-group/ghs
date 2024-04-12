package ch.usi.si.seart.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.data.domain.Sort;

import java.io.IOException;

public class SortSerializer extends StdSerializer<Sort> {

    public static final SortSerializer INSTANCE = new SortSerializer();

    private SortSerializer() {
        super(Sort.class);
    }

    @Override
    public void serialize(Sort value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartArray();
        for (Sort.Order order : value) {
            gen.writeStartObject();
            gen.writeStringField("property", order.getProperty());
            gen.writeStringField("direction", order.getDirection().toString());
            gen.writeEndObject();
        }
        gen.writeEndArray();
    }

    @Override
    public Class<Sort> handledType() {
        return Sort.class;
    }
}
