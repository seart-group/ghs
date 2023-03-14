package usi.si.seart.service;

import javax.validation.constraints.NotNull;
import java.util.Optional;

public interface MetricLanguageService {

    /**
     * Returns the ID of the provided metric language if it exists in the database.
     * @param language_name the language name (e.g "C++")
     * @return ID of the provided language if it exists in the database.
     */
    Optional<Long> getLanguageId(@NotNull String language_name);
}