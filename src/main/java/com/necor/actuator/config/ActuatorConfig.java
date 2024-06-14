package com.necor.actuator.config;

import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.health.HealthProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementWebServerFactoryCustomizer;
import org.springframework.boot.actuate.context.ShutdownEndpoint;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class ActuatorConfig {

    @Bean
    @Primary
    public WebEndpointProperties webEndpointProperties(Environment env) {
        WebEndpointProperties properties = new WebEndpointProperties();
        properties.setBasePath("/" + env.getProperty("spring.application.name"));
        properties.getExposure().setInclude(Stream.of("health", "info", "shutdown").collect(Collectors.toSet()));
        return properties;
    }

    @Bean
    @Primary
    public HealthEndpointProperties healthEndpointProperties() {
        HealthEndpointProperties properties = new HealthEndpointProperties();
        Map<String, Integer> httpMapping = Stream.of(new Object[][] {
                { "FATAL", 503 },
                { "DOWN", 503 }
        }).collect(Collectors.toMap(data -> (String) data[0], data -> (Integer) data[1]));

        properties.getStatus().setOrder(Arrays.asList("FATAL","DOWN","OUT_OF_SERVICE","UNKNOWN","UP"));
        properties.getStatus().getHttpMapping().putAll(httpMapping);
        properties.setShowDetails(HealthProperties.Show.ALWAYS);
        return properties;
    }

    @Bean
    @Primary
//    @ConditionalOnProperty(name = "management.endpoint.shutdown.enabled", havingValue = "true", matchIfMissing = false)
    public ShutdownEndpoint shutdownEndpoint() {
        return new ShutdownEndpoint();
    }

    @Bean
    @Primary
    public InfoContributor envInfoContributor() {
        return builder -> builder.withDetail("env", System.getenv());
    }

    @Bean
    @Primary
    public InfoEndpoint infoEndpoint(InfoContributor envInfoContributor) {
        return new InfoEndpoint(Collections.singletonList(envInfoContributor));
    }
}
