package ch.usi.si.seart.http;

import ch.usi.si.seart.config.properties.ClientURLProperties;
import ch.usi.si.seart.exception.ClientURLException;
import ch.usi.si.seart.exception.TerminalExecutionException;
import ch.usi.si.seart.io.ExternalProcess;
import ch.usi.si.seart.stereotype.Connector;
import lombok.AccessLevel;
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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ClientURLConnector {

    Duration connectTimeout;

    @Autowired
    public ClientURLConnector(ClientURLProperties properties) {
        this.connectTimeout = properties.getConnectTimeoutDuration();
    }

    public boolean ping(URL url) throws ClientURLException {
        try {
            String[] command = {
                "curl", "-Is",
                "--connect-timeout",
                String.valueOf(connectTimeout.toSeconds()),
                "--fail-with-body",
                "--show-error",
                url.toString()
            };
            ExternalProcess process = new ExternalProcess(command);
            log.trace("Pinging:   {}", url);
            ExternalProcess.Result result = process.execute(connectTimeout.toMillis());
            return switch (result.code()) {
                case 0 -> true;
                case 6 -> throw new UnknownHostException("Could not resolve host address!");
                case 7 -> throw new ConnectException("Connection to host failed!");
                default -> false;
            };
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ClientURLException("Cancelled for: " + url, ex);
        } catch (TimeoutException ex) {
            throw new ClientURLException("Timeout out for: " + url, ex);
        } catch (TerminalExecutionException ex) {
            throw new ClientURLException("Failed for: " + url, ex);
        } catch (UnknownHostException | ConnectException ex) {
            throw new ClientURLException(ex);
        }
    }
}
