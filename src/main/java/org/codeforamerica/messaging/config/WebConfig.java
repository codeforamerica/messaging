package org.codeforamerica.messaging.config;

import org.codeforamerica.messaging.config.filters.TrustedEndpointsFilter;
import org.codeforamerica.messaging.config.filters.TwilioRequestValidatorFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${server.trustedPort:null}")
    private int trustedPort;

    @Value("${server.trustedPathPrefix:null}")
    private String trustedPathPrefix;

    @Value("${twilio.auth.token}")
    private String twilioAuthToken;

    @Bean
    public FilterRegistrationBean<TrustedEndpointsFilter> trustedEndpointsFilter() {
        return new FilterRegistrationBean<>(new TrustedEndpointsFilter(trustedPort, trustedPathPrefix));
    }

    @Bean
    @Profile("!test && !ci")
    public FilterRegistrationBean<TwilioRequestValidatorFilter> twilioRequestValidatorFilter() {
        FilterRegistrationBean<TwilioRequestValidatorFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new TwilioRequestValidatorFilter(twilioAuthToken));
        filterRegistrationBean.addUrlPatterns("/public/twilio_callbacks/*");
        return filterRegistrationBean;
    }
}
