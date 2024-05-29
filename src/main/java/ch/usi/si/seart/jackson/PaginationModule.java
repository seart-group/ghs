package ch.usi.si.seart.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;

public class PaginationModule extends SimpleModule {

    public PaginationModule() {
        super(PaginationModule.class.getName());
        addSerializer(SortSerializer.INSTANCE);
        addSerializer(PageSerializer.INSTANCE);
    }
}
