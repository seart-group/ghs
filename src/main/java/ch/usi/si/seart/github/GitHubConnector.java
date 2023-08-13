package ch.usi.si.seart.github;

import ch.usi.si.seart.exception.github.GitHubConnectorException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor(onConstructor_ = @Autowired)
public abstract class GitHubConnector<R extends Response> {

    RetryTemplate retryTemplate;

    protected R execute(Callback<R> callback) {
        try {
            return retryTemplate.execute(callback);
        } catch (Exception ex) {
            throw new GitHubConnectorException(ex);
        }
    }
}
