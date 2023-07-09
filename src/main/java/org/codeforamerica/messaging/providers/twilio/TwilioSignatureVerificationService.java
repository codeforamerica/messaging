package org.codeforamerica.messaging.providers.twilio;

import com.twilio.security.RequestValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TwilioSignatureVerificationService {
    @Value("${twilio.auth.token}")
    private String TWILIO_AUTH_TOKEN;

    public boolean verifySignature(HttpServletRequest httpRequest) {
        RequestValidator requestValidator = new RequestValidator(TWILIO_AUTH_TOKEN);
        return requestValidator.validate(
                getRequestUrlAndQueryString(httpRequest),
                extractPostParams(httpRequest),
                httpRequest.getHeader("X-Twilio-Signature"));
    }

    private String getRequestUrlAndQueryString(HttpServletRequest request) {
        String queryString = request.getQueryString();
        // Load balancers re-direct https requests to http, must recreate the original https request url
        // https://stackoverflow.com/a/43593017
        String requestUrl = StringUtils.replaceOnce(
                request.getRequestURL().toString(), "http", "https");
        return queryString == null || queryString.isBlank() ? requestUrl : requestUrl + "?" + queryString;
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
