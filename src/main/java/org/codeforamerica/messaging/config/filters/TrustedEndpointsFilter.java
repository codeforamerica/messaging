package org.codeforamerica.messaging.config.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TrustedEndpointsFilter implements Filter {

    private int trustedPort = 0;
    private String trustedPathPrefix;
    private final Logger log = LoggerFactory.getLogger(getClass().getName());

    public TrustedEndpointsFilter(String trustedPort, String trustedPathPrefix) {
        if (trustedPort != null && trustedPathPrefix != null && !"null".equals(trustedPathPrefix)) {
            this.trustedPort = Integer.parseInt(trustedPort);
            this.trustedPathPrefix = trustedPathPrefix;
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        var httpRequest = (HttpServletRequest) servletRequest;
        var httpResponse = (HttpServletResponse) servletResponse;
        log.info("Request Port: {}, URI: {}", httpRequest.getLocalPort(), httpRequest.getRequestURI());
        if (trustedPort != 0) {
            if (isRequestForCallbackEndpoint(httpRequest) && servletRequest.getLocalPort() != trustedPort) {
                log.warn("denying request for trusted endpoint on untrusted port");
                httpResponse.setStatus(404);
                httpResponse.getOutputStream().close();
                return;
            }

            if (!isRequestForCallbackEndpoint(httpRequest) && servletRequest.getLocalPort() == trustedPort) {
                log.warn("denying request for untrusted endpoint on trusted port");
                httpResponse.setStatus(404);
                httpResponse.getOutputStream().close();
                return;
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private boolean isRequestForCallbackEndpoint(HttpServletRequest httpRequest) {
        return httpRequest.getRequestURI().startsWith(trustedPathPrefix);
    }
}
