package ch.usi.si.seart.repository.operation;

public interface Operation {

    String name();

    default RuntimeException toRuntimeException() {
        return new UnsupportedOperationException(
                "Operation: [" + name() + "] not supported!"
        );
    }
}
