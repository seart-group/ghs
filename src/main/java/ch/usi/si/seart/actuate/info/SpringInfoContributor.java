package ch.usi.si.seart.actuate.info;

import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.SpringVersion;

import java.util.LinkedHashMap;
import java.util.Map;

public class SpringInfoContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> spring = new LinkedHashMap<>(2);
        spring.put("core-version", SpringVersion.getVersion());
        spring.put("boot-version", SpringBootVersion.getVersion());
        builder.withDetail("spring", spring);
    }
}
