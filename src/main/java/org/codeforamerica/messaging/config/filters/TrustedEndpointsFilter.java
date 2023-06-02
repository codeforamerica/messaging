package org.codeforamerica.messaging.config.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class TrustedEndpointsFilter implements Filter {

    private final int trustedPort;
    private final String trustedPathPrefix;

    public TrustedEndpointsFilter(int trustedPort, String trustedPathPrefix) {
        this.trustedPort = trustedPort;
        this.trustedPathPrefix = trustedPathPrefix;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        var httpRequest = (HttpServletRequest) servletRequest;
        var httpResponse = (HttpServletResponse) servletResponse;
        if (isRequestForTrustedEndpoint(httpRequest) && servletRequest.getLocalPort() != trustedPort) {
            log.warn("Denying request for trusted endpoint {} on untrusted port {}", httpRequest.getRequestURI(), httpRequest.getLocalPort());
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        } else if (!isRequestForTrustedEndpoint(httpRequest) && servletRequest.getLocalPort() == trustedPort) {
            log.warn("Denying request for untrusted endpoint {} on trusted port {}", httpRequest.getRequestURI(), httpRequest.getLocalPort());
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private boolean isRequestForTrustedEndpoint(HttpServletRequest httpRequest) {
        return httpRequest.getRequestURI().startsWith(trustedPathPrefix);
    }
}
