package com.klb.account_service.repository.httpclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.klb.account_service.dto.request.KeycloakUserCreationRequest;
import com.klb.account_service.dto.request.TokenExchangeRequest;
import com.klb.account_service.dto.response.TokenExchangeResponse;

import reactor.core.publisher.Mono;

@Component
public class KeycloakClient {

    private final WebClient keycloakWebClient;

    public KeycloakClient(@Qualifier("keycloakWebClient") WebClient keycloakWebClient) {
        this.keycloakWebClient = keycloakWebClient;
    }

    public Mono<TokenExchangeResponse> exchangeToken(TokenExchangeRequest request) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", request.getGrant_type());
        formData.add("client_id", request.getClient_id());
        formData.add("client_secret", request.getClient_secret());
        if (request.getScope() != null) {
            formData.add("scope", request.getScope());
        }

        return keycloakWebClient
                .post()
                .uri("/realms/bank_account/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(TokenExchangeResponse.class);
    }

    public Mono<Boolean> createUser(String authorization, KeycloakUserCreationRequest request) {
        return keycloakWebClient
                .post()
                .uri("/admin/realms/bank_account/users")
                .header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchangeToMono(response -> {
                    if (response.statusCode() == HttpStatus.CREATED) {
                        return Mono.just(true);
                    } else {
                        return response.bodyToMono(String.class).flatMap(errorBody -> Mono.just(false));
                    }
                })
                .onErrorReturn(false);
    }
}
