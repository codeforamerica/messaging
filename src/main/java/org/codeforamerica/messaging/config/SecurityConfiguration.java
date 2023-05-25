package org.codeforamerica.messaging.config;

import com.twilio.rest.serverless.v1.service.environment.Log;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.springframework.security.authorization.AuthenticatedAuthorizationManager.authenticated;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@Slf4j
public class SecurityConfiguration {

    @Value("${allowed-ip-addresses}")
    String allowedIpAddresses;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/**").access(AuthorizationManagers.allOf(authenticated(), authorizeApiRequest()))
                        .requestMatchers("/api/v1/**").authenticated()
                        .requestMatchers("/error/**").authenticated()
                        .requestMatchers("/mailgun_callbacks/**").permitAll()
                        .requestMatchers("/twilio_callbacks/**").permitAll()
                        .requestMatchers("/docs/**").permitAll()
                        .requestMatchers("/v3/**").permitAll()
                        .anyRequest().denyAll()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(withDefaults());
        return http.build();
    }

    private AuthorizationManager<RequestAuthorizationContext> authorizeApiRequest() {
        return (authentication, context) -> {
            HttpServletRequest request = context.getRequest();
            boolean ipAddressAllowed = Arrays.stream(allowedIpAddresses.split(","))
                    .anyMatch(allowedIpAddress -> {
                        IpAddressMatcher ipAddressMatcher = new IpAddressMatcher(allowedIpAddress);
                        log.info("Looking for: " + allowedIpAddress);
                        String xForwardedFor = request.getHeader("X-Forwarded-For");
                        log.info("X-Forwarded-For is: " + xForwardedFor);
                        boolean matchesXForwardedFor = false;
                        if (xForwardedFor != null) {
                            matchesXForwardedFor = Arrays.stream(xForwardedFor.split(", "))
                                    .anyMatch(xForwardedForIp -> {
                                        log.info("*" + xForwardedForIp + "*");
                                        return ipAddressMatcher.matches(xForwardedForIp);
                                    });
                        }
                        log.info("RemoteAddr: " + request.getRemoteAddr());
                        boolean matchesRemoteAddr = ipAddressMatcher.matches(request.getRemoteAddr());
                        log.info("Returning: " + (matchesRemoteAddr || matchesXForwardedFor));
                        return matchesRemoteAddr || matchesXForwardedFor;
                    });
            return new AuthorizationDecision(ipAddressAllowed);
        };
    }
}
