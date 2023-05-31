package org.codeforamerica.messaging.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static org.springframework.security.authorization.AuthenticatedAuthorizationManager.authenticated;
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
                        .requestMatchers("/api/v1/**").access(AuthorizationManagers.allOf(authenticated(), this::validIP))
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

    private AuthorizationDecision validIP(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
            HttpServletRequest request = context.getRequest();
            List<String> requestAddresses = getRequestAddresses(request);
            boolean ipAddressAllowed = Arrays.stream(allowedIpAddresses.split(","))
                    .anyMatch(allowedIpAddress -> {
                        IpAddressMatcher ipAddressMatcher = new IpAddressMatcher(allowedIpAddress);
                        return requestAddresses.stream().anyMatch(ipAddressMatcher::matches);
                    });
            return new AuthorizationDecision(ipAddressAllowed);
    }

    private List<String> getRequestAddresses(HttpServletRequest request) {
        List<String> requestAddresses = new ArrayList<>(List.of(request.getRemoteAddr()));
        String forwardedForAddress = getCorrectForwardedForAddress(request);
        if (forwardedForAddress != null) {
            requestAddresses.add(forwardedForAddress);
        }
        return requestAddresses;
    }

    private String getCorrectForwardedForAddress(HttpServletRequest request) {
        /* Based on https://www.stackhawk.com/blog/do-you-trust-your-x-forwarded-for-header/
        and https://www.aptible.com/docs/http-request-headers, it looks like we
        should only examine the penultimate entry in the case of Aptible with
        ALB. */
        
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null) {
            String[] forwardedForAddresses = xForwardedFor.split(", ?");
            if (forwardedForAddresses.length > 1) {
                return forwardedForAddresses[forwardedForAddresses.length - 2];
            }
        }
        return null;
    }
}
