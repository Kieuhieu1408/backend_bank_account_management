package com.klb.gateway.controller;

import com.klb.gateway.dto.ApiResponse;
import com.klb.gateway.dto.request.AuthenticationRequest;
import com.klb.gateway.dto.response.AuthenticationResponse;
import com.klb.gateway.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/token")
    public Mono<ApiResponse<AuthenticationResponse>> authenticate(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return result.map(response -> ApiResponse.<AuthenticationResponse>builder()
                .result(response)
                .build());
    }
}
