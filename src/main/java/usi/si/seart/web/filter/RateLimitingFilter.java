package usi.si.seart.web.filter;

import com.google.common.util.concurrent.RateLimiter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("UnstableApiUsage")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RateLimitingFilter extends OncePerRequestFilter {

    ConcurrentRateLimiterMap rateLimiters = new ConcurrentRateLimiterMap();

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class ConcurrentRateLimiterMap {

        ConcurrentReferenceHashMap<String, RateLimiter> map = new ConcurrentReferenceHashMap<>();

        private RateLimiter get(String ip) {
            return map.compute(ip, (k, v) -> v == null ? RateLimiter.create(10) : v);
        }
    }

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain
    ) throws ServletException, IOException {
        String ip = request.getRemoteAddr();
        RateLimiter rateLimiter = rateLimiters.get(ip);

        if (!rateLimiter.tryAcquire(1, TimeUnit.SECONDS)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            return;
        }

        filterChain.doFilter(request, response);
    }
}
