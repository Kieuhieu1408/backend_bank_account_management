package com.klb.gateway.service;

import com.klb.gateway.dto.request.AuthenticationRequest;
import com.klb.gateway.dto.response.AuthenticationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final WebClient webClient;

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String defaultClientId;

    @Value("${keycloak.credentials.secret}")
    private String defaultClientSecret;

    public Mono<AuthenticationResponse> authenticate(AuthenticationRequest request) {
        log.info("Starting authentication process for user: {}", request.getUsername());

        // Validate input
        if (request.getUsername() == null || request.getPassword() == null) {
            log.error("Username or password is null");
            return Mono.error(new IllegalArgumentException("Username and password are required"));
        }

        String tokenUrl = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        MultiValueMap<String, String> formData = buildFormData(request);

        return webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(AuthenticationResponse.class)
                .doOnSuccess(response -> {
                    log.info("Authentication successful for user: {}", request.getUsername());
                    log.debug("Token expires in: {} seconds", response.getExpiresIn());
                })
                .onErrorResume(WebClientResponseException.class, this::handleWebClientException)
                .onErrorResume(Exception.class, this::handleGenericException);
    }

    private MultiValueMap<String, String> buildFormData(AuthenticationRequest request) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        // Use request values or fall back to defaults
        formData.add("grant_type", request.getGrantType() != null ? request.getGrantType() : "password");
        formData.add("client_id", request.getClientId() != null ? request.getClientId() : defaultClientId);
        formData.add("client_secret", request.getClientSecret() != null ? request.getClientSecret() : defaultClientSecret);
        formData.add("username", request.getUsername());
        formData.add("password", request.getPassword());
        formData.add("scope", request.getScope() != null ? request.getScope() : "openid");

        log.debug("Form data prepared with client_id: {} and grant_type: {}",
                 formData.getFirst("client_id"), formData.getFirst("grant_type"));

        return formData;
    }

    private Mono<AuthenticationResponse> handleWebClientException(WebClientResponseException ex) {
        log.error("Keycloak authentication failed with status: {} and body: {}",
                 ex.getStatusCode(), ex.getResponseBodyAsString());

        String errorMessage = switch (ex.getStatusCode().value()) {
            case 400 -> "Invalid credentials or request parameters";
            case 401 -> "Invalid username or password";
            case 403 -> "Access forbidden";
            case 404 -> "Authentication endpoint not found";
            case 500 -> "Keycloak server error";
            default -> "Authentication failed: " + ex.getMessage();
        };

        return Mono.error(new RuntimeException(errorMessage));
    }

    private Mono<AuthenticationResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error during authentication: {}", ex.getMessage(), ex);
        return Mono.error(new RuntimeException("Authentication service unavailable"));
    }
}
