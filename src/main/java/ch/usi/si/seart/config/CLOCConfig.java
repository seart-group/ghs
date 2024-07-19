package ch.usi.si.seart.config;

import ch.usi.si.seart.cloc.CLOC;
import ch.usi.si.seart.config.properties.CLOCProperties;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.util.unit.DataSize;

import java.time.Duration;

@Configuration
public class CLOCConfig {

    @Bean
    InitializingBean clocInitializingBean(JsonMapper jsonMapper) {
        return () -> CLOC.setOutputMapper(jsonMapper);
    }

    @Bean
    @Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CLOC.Builder clocCommandBuilder(CLOCProperties clocProperties) {
        DataSize size = clocProperties.getMaxFileSize();
        int megabytes = Math.toIntExact(size.toMegabytes());
        Duration duration = clocProperties.getTimeoutDuration();
        int seconds = Math.toIntExact(duration.getSeconds());
        return CLOC.command()
                .maxFileSize(megabytes)
                .timeout(seconds);
    }
}
