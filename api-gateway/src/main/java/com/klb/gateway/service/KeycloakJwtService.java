package com.klb.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakJwtService {

    private final ReactiveJwtDecoder jwtDecoder;

    public Mono<Boolean> validateToken(String token) {
        return jwtDecoder.decode(token)
                .map(jwt -> {
                    log.info("Token validated successfully for subject: {}", jwt.getSubject());
                    return true;
                })
                .onErrorResume(throwable -> {
                    log.error("Token validation failed: {}", throwable.getMessage());
                    return Mono.just(false);
                });
    }

    public Mono<Jwt> decodeToken(String token) {
        return jwtDecoder.decode(token);
    }
}
