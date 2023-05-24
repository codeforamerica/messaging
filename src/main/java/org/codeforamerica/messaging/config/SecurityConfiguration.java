package org.codeforamerica.messaging.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfiguration {

    @Value("${allowed-ip-addresses}")
    String allowedIpAddresses;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/**").access(authorizeApiRequest())
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
                        return ipAddressMatcher.matches(request.getRemoteAddr())
                                || ipAddressMatcher.matches(request.getHeader("X-Forwarded-For"));
                    });
            return new AuthorizationDecision(
                    authentication.get().isAuthenticated()
                            && ipAddressAllowed
            );
        };
    }

    /**
     Example Output: "isAuthenticated() and (hasIpAddress('127.0.0.1') or hasIpAddress('::1'))"
     */
    private String createExpressionString() {
        return Arrays.stream(allowedIpAddresses.split(","))
                .map(String::strip)
                .collect(Collectors.joining(
                        "') or hasIpAddress('",
                        "isAuthenticated() and (hasIpAddress('",
                        "'))"));
    }
}
