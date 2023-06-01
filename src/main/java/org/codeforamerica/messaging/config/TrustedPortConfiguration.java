package org.codeforamerica.messaging.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration(proxyBeanMethods = false)
public class TrustedPortConfiguration {
    @Value("${server.trustedPort:null}")
    private String trustedPort;


    @Bean
        public WebServerFactoryCustomizer<TomcatServletWebServerFactory> connectorCustomizer() {
            return (tomcat) -> tomcat.addAdditionalTomcatConnectors(createConnector());
        }

        private Connector createConnector() {
            Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
            connector.setPort(Integer.parseInt(trustedPort));
            return connector;
        }
}
