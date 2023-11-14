package ch.usi.si.seart.http;

import ch.usi.si.seart.config.properties.ClientURLProperties;
import ch.usi.si.seart.exception.ClientURLException;
import ch.usi.si.seart.exception.TerminalExecutionException;
import ch.usi.si.seart.io.ExternalProcess;
import ch.usi.si.seart.stereotype.Connector;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.ConnectException;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Slf4j
@Connector(command = "curl")
@AllArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ClientURLConnector {

    ClientURLProperties clientURLProperties;

    public boolean ping(URL url) throws ClientURLException {
        try {
            Duration duration = clientURLProperties.getConnectTimeoutDuration();
            String[] command = {
                "curl", "-Is",
                "--connect-timeout",
                String.valueOf(duration.toSeconds()),
                "--fail-with-body",
                "--show-error",
                url.toString()
            };
            ExternalProcess process = new ExternalProcess(command);
            log.trace("Pinging:   {}", url);
            ExternalProcess.Result result = process.execute(duration.toMillis());
            int code = result.getCode();
            return switch (code) {
                case 0 -> true;
                case 6 -> throw new UnknownHostException("Could not resolve host address!");
                case 7 -> throw new ConnectException("Connection to host failed!");
                default -> false;
            };
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ClientURLException("Timed out for: " + url, ex);
        } catch (TerminalExecutionException | TimeoutException ex) {
            throw new ClientURLException("Failed for: " + url, ex);
        } catch (UnknownHostException | ConnectException ex) {
            throw new ClientURLException(ex);
        }
    }
}
