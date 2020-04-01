package com.dabico.gseapp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;

public class PropertiesExtractor {

    static final Logger logger = LoggerFactory.getLogger(PropertiesExtractor.class);

    private static Properties properties;
    static {
        properties = new Properties();
        URL url = PropertiesExtractor.class.getClassLoader().getResource("application.properties");
        try {
            properties.load(new FileInputStream(Objects.requireNonNull(url).getPath()));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public static String getCrawlInterval(){
        return properties.getProperty("app.crawl.interval");
    }
}
