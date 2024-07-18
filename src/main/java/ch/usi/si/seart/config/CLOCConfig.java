package ch.usi.si.seart.config;

import ch.usi.si.seart.cloc.CLOCCommand;
import ch.usi.si.seart.config.properties.CLOCProperties;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.time.Duration;

@Configuration
public class CLOCConfig {

    @Bean
    InitializingBean clocInitializingBean(JsonMapper jsonMapper) {
        return () -> CLOCCommand.setOutputMapper(jsonMapper);
    }

    @Bean
    @Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CLOCCommand.Builder clocCommandBuilder(CLOCProperties properties) {
        Duration duration = properties.getAnalysisTimeoutDuration();
        int seconds = Math.toIntExact(duration.getSeconds());
        return CLOCCommand.create().withTimeout(seconds);
    }
}
