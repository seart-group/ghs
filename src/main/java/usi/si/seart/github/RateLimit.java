package usi.si.seart.github;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.Instant;

/**
 * Models a rate limit for the GitHub API.
 * Contains two {@code Resource} objects: {@code core} and {@code search},
 * each representing the rate limits for two different types of API calls.
 *
 * @author Ozren Dabić
 * @see <a href="https://docs.github.com/en/rest/rate-limit?apiVersion=2022-11-28">Rate limit</a>
 * @see <a href="https://docs.github.com/en/rest/overview/resources-in-the-rest-api?apiVersion=2022-11-28">Resources in the REST API</a>
 */
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RateLimit {

    Resource coreResource;
    Resource searchResource;

    /**
     * Calculates the maximum wait time (in seconds) between API
     * requests based on the wait time for both {@code core} and {@code search} resources.
     *
     * @return Maximum wait time in seconds between core and search.
     */
    public long getMaxWaitSeconds() {
        return Math.max(
                coreResource.getWaitSeconds(),
                searchResource.getWaitSeconds()
        );
    }

    /**
     * Outputs the maximum wait duration in a more human-readable format.
     *
     * @return The wait duration expressed as a sentence.
     */
    public String getMaxWaitReadable() {
        return DurationFormatUtils.formatDurationWords(getMaxWaitSeconds() * 1000, true, false);
    }

    /**
     * Determines whether any of the API resources have
     * been exhausted in terms of permitted requests.
     *
     * @return true if even a single resource has been exhausted, false otherwise
     */
    public boolean anyExceeded() {
        return coreResource.isExceeded() || searchResource.isExceeded();
    }

    /**
     * Represents the rate limit information for a single API call type.
     * Contains information about the maximum number of allowed API calls,
     * the number of remaining API calls associated with the client,
     * and the time in Unix seconds at which the rates will reset.
     */
    @Getter
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class Resource {

        int limit;
        int remaining;
        long reset;

        /**
         * Calculates the number of seconds to wait before making another
         * API call, based on the number of remaining calls and reset time.
         *
         * @return
         * Number of seconds until the rate limits reset,
         * if there are no remaining calls, 0 otherwise.
         */
        public long getWaitSeconds() {
            return (remaining == 0)
                    ? reset - Instant.now().getEpochSecond()
                    : 0;
        }

        /**
         * @return true if requests can currently not be made for this API type, false otherwise
         */
        public boolean isExceeded() {
            return remaining <= 0;
        }

        @Override
        public String toString() {
            String format = "Limit: %d, Remaining: %d, Wait: %d seconds";
            return String.format(format, limit, remaining, getWaitSeconds());
        }
    }
}
