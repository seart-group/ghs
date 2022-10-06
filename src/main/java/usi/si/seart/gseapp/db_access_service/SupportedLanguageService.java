package usi.si.seart.gseapp.db_access_service;

import usi.si.seart.gseapp.model.SupportedLanguage;

import java.util.List;

public interface SupportedLanguageService {
    List<SupportedLanguage> getAll();
    SupportedLanguage create(SupportedLanguage language);
}
