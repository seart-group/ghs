package ch.usi.si.seart.github;

import ch.usi.si.seart.exception.github.GitHubConnectorException;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;

@Accessors(makeFinal = true)
@Setter(onMethod_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class GitHubConnector<R extends JsonResponse> {

    RetryTemplate retryTemplate;

    protected R execute(Callback<R> callback) {
        try {
            return retryTemplate.execute(callback);
        } catch (Exception ex) {
            throw new GitHubConnectorException(ex);
        }
    }
}
