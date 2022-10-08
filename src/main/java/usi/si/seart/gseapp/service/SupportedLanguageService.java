package usi.si.seart.gseapp.service;

import usi.si.seart.gseapp.model.SupportedLanguage;

import java.util.List;

public interface SupportedLanguageService {
    List<SupportedLanguage> getAll();
    SupportedLanguage create(SupportedLanguage language);
}
