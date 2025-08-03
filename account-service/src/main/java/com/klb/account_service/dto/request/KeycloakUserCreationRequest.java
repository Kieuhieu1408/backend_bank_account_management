package com.klb.account_service.dto.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeycloakUserCreationRequest {
    private String username;
    private boolean enabled;
    private String email;
    private boolean emailVerified;
    private String firstName;
    private String lastName;
    private List<KeycloakCredential> credentials;
}
