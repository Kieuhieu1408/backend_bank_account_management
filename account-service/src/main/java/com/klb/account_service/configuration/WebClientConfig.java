package com.klb.account_service.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;

    @Bean
    public WebClient keycloakWebClient() {
        return WebClient.builder().baseUrl(keycloakUrl).build();
    }
}
