package com.dabico.gseapp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class PropertiesExtractor {

    static final Logger logger = LoggerFactory.getLogger(PropertiesExtractor.class);

    private static Properties properties;
    static {
        properties = new Properties();
        try {
            properties.load(new FileInputStream(new ClassPathResource("src/main/resources/application.properties").getPath()));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public static Date getStartDate(){
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(properties.getProperty("app.crawl.startdate"));
        } catch (ParseException ex) {
            return null;
        }
    }
}
