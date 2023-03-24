package usi.si.seart.service;

import usi.si.seart.model.MetricLanguage;

import javax.validation.constraints.NotNull;

public interface MetricLanguageService {

    MetricLanguage getOrCreateMetricLanguage(@NotNull String languageName);

}