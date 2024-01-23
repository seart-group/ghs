package ch.usi.si.seart.web.filter;

import ch.usi.si.seart.web.Headers;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;


public class Slf4jMDCLoggingFilter extends OncePerRequestFilter {

    private static final String KEY = "LoggingFilter.UUID";

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {
        String value = UUID.randomUUID().toString();
        MDC.put(KEY, value);
        try {
            response.setHeader(Headers.X_REQUEST_ID, value);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(KEY);
        }
    }

    @Override
    protected boolean isAsyncDispatch(@NotNull HttpServletRequest request) {
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }
}
