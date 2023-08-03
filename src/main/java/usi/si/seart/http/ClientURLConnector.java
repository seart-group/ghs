package usi.si.seart.http;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import usi.si.seart.exception.ClientURLException;
import usi.si.seart.exception.TerminalExecutionException;
import usi.si.seart.io.ExternalProcess;

import java.net.ConnectException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(onConstructor_ = @Autowired)
public class ClientURLConnector {

    public boolean ping(URL url) throws ClientURLException {
        try {
            String[] command = {
                "curl", "-Is",
                "--connect-timeout", "60",
                "--fail-with-body",
                "--show-error",
                url.toString()
            };
            ExternalProcess process = new ExternalProcess(command);
            log.trace("Pinging:   {}", url);
            ExternalProcess.Result result = process.execute(1, TimeUnit.MINUTES);
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
