package org.codeforamerica.messaging.config.filters;

import com.twilio.security.RequestValidator;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class TwilioRequestValidatorFilter implements Filter {

    private final RequestValidator requestValidator;

    public TwilioRequestValidatorFilter(String twilioAuthToken) {
        this.requestValidator = new RequestValidator(twilioAuthToken);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        var httpRequest = (HttpServletRequest) servletRequest;
        var httpResponse = (HttpServletResponse) servletResponse;
        if (!requestValidator.validate(
                getRequestUrlAndQueryString(httpRequest),
                extractPostParams(httpRequest),
                httpRequest.getHeader("X-Twilio-Signature"))) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else {
            filterChain.doFilter(httpRequest, httpResponse);
        }
    }

    private String getRequestUrlAndQueryString(HttpServletRequest request) {
        String queryString = request.getQueryString();
        String requestUrl = request.getRequestURL().toString();
        if (queryString != null && !queryString.isBlank()) {
            return requestUrl + "?" + queryString;
        }
        return requestUrl;
    }

    private Map<String, String> extractPostParams(HttpServletRequest request) {
        List<String> queryStringKeys = getQueryStringKeys(request.getQueryString());
        return request.getParameterMap().entrySet().stream()
                .filter(e -> !queryStringKeys.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()[0]));
    }

    private List<String> getQueryStringKeys(String queryString) {
        if (queryString != null && !queryString.isBlank()) {
            return Arrays.stream(queryString.split("&"))
                    .map(pair -> pair.split("=")[0])
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}