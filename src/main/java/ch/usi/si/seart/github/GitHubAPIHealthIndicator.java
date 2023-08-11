package ch.usi.si.seart.github;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.io.IOException;
import java.net.Socket;

@Slf4j
public class GitHubAPIHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try (Socket ignored = new Socket(Endpoint.ROOT.getHost(), 80)) {
            return Health.up().build();
        } catch (IOException ex) {
            log.warn("Could not connect to the GitHub API", ex);
            return Health.down(ex).build();
        }
    }
}
