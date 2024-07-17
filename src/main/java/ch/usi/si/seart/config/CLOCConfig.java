package ch.usi.si.seart.config;

import ch.usi.si.seart.cloc.CLOCCommand;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CLOCConfig {

    @Bean
    InitializingBean clocInitializingBean(JsonMapper jsonMapper) {
        return () -> CLOCCommand.setOutputMapper(jsonMapper);
    }
}
