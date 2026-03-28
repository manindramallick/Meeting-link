package com.demo.filter;

import com.demo.config.TokenProperties;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.demo.constants.HeaderConstants.ENV_HEADER;

/**
 * OncePerRequestFilter that ensures the "env" header is always populated.

 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EnvironmentFilter extends OncePerRequestFilter {

    private final TokenProperties tokenProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain)
            throws ServletException, IOException {

        String env = request.getHeader(ENV_HEADER);

        if (env == null || env.isBlank()) {
            String defaultEnv = tokenProperties.getEnvironmentDefault();
            log.info("EnvironmentFilter: 'env' not in header — using config default '{}'", defaultEnv);
            request = new MutableHttpServletRequest(request, ENV_HEADER, defaultEnv);
        } else {
            log.debug("EnvironmentFilter: 'env' header already present = {}", env);
        }

        filterChain.doFilter(request, response);
    }


    private static class MutableHttpServletRequest extends HttpServletRequestWrapper {

        private final Map<String, String> extraHeaders;

        MutableHttpServletRequest(HttpServletRequest request, String name, String value) {
            super(request);
            this.extraHeaders = new HashMap<>();
            this.extraHeaders.put(name.toLowerCase(), value);
        }

        @Override
        public String getHeader(String name) {
            String override = extraHeaders.get(name.toLowerCase());
            return override != null ? override : super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            String override = extraHeaders.get(name.toLowerCase());
            if (override != null) {
                return Collections.enumeration(List.of(override));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> names = Collections.list(super.getHeaderNames());
            extraHeaders.keySet().forEach(k -> {
                if (!names.contains(k)) names.add(k);
            });
            return Collections.enumeration(names);
        }
    }
}
