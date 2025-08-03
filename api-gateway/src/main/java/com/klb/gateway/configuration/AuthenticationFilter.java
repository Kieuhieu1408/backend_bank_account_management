package com.klb.gateway.configuration;

import com.klb.gateway.dto.ApiResponse;
import com.klb.gateway.service.KeycloakJwtService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE, makeFinal = true)
public class AuthenticationFilter implements GlobalFilter, Ordered {
    KeycloakJwtService keycloakJwtService;
    ObjectMapper objectMapper;

    private final String[] PUBLIC_ENDPOINTS = {
            "/auth/.*",
            "/api/v1/identity/auth/.*",
            "/api/v1/identity/users/registration",
            "/api/v1/notification/email/send",
            "/api/v1/file/media/download/.*"
    };

    @Value("${app.api-prefix}")
    @NonFinal
    private String apiPrefix;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Enter authentication filter....");

        if (isPublicEndpoint(exchange.getRequest()))
            return chain.filter(exchange);

        // Get token from authorization header
        List<String> authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (CollectionUtils.isEmpty(authHeader))
            return unauthenticated(exchange.getResponse());

        String token = authHeader.getFirst().replace("Bearer ", "");
        log.info("Token: {}", token);

        return keycloakJwtService.validateToken(token).flatMap(isValid -> {
            if (isValid) {
                return keycloakJwtService.decodeToken(token).flatMap(jwt -> {
                    // Extract roles từ realm_access object
                    String roles = "";
                    try {
                        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
                        if (realmAccess != null && realmAccess.containsKey("roles")) {
                            @SuppressWarnings("unchecked")
                            List<String> rolesList = (List<String>) realmAccess.get("roles");
                            roles = String.join(",", rolesList);
                        }
                    } catch (Exception e) {
                        log.warn("Could not extract roles from token: {}", e.getMessage());
                    }

                    // Thêm thông tin user vào header để các service downstream sử dụng
                    ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                            .header("X-User-Id", jwt.getSubject())
                            .header("X-User-Email", jwt.getClaimAsString("email"))
                            .header("X-User-Name", jwt.getClaimAsString("preferred_username"))
                            .header("X-User-Roles", roles)
                            .build();

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                }).onErrorResume(decodeError -> {
                    log.error("Error decoding token: {}", decodeError.getMessage(), decodeError);
                    return unauthenticated(exchange.getResponse());
                });
            } else {
                return unauthenticated(exchange.getResponse());
            }
        }).onErrorResume(throwable -> {
            log.error("Authentication error: {}", throwable.getMessage(), throwable);
            return unauthenticated(exchange.getResponse());
        });
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private boolean isPublicEndpoint(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        log.info("Checking path: {} against public endpoints", path);

        // Kiểm tra trực tiếp cho /auth endpoints (không có api prefix)
        if (path.startsWith("/auth/")) {
            log.info("Path {} is public (auth endpoint)", path);
            return true;
        }

        // Kiểm tra các endpoints khác với api prefix
        return Arrays.stream(PUBLIC_ENDPOINTS)
                .anyMatch(pattern -> {
                    String fullPattern = apiPrefix + pattern;
                    boolean matches = path.matches(fullPattern);
                    log.debug("Checking {} against pattern {}: {}", path, fullPattern, matches);
                    return matches;
                });
    }

    Mono<Void> unauthenticated(ServerHttpResponse response) {
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(1401)
                .message("Unauthenticated")
                .build();

        String body = null;
        try {
            body = objectMapper.writeValueAsString(apiResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }
}
