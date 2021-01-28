package usi.si.seart.gseapp.db_access_service;

import usi.si.seart.gseapp.dto.StringList;
import usi.si.seart.gseapp.dto.SupportedLanguageDto;
import usi.si.seart.gseapp.model.SupportedLanguage;

public interface SupportedLanguageService {
    StringList getAll();
    SupportedLanguageDto create(SupportedLanguage language);
    void delete(Long id);
}
