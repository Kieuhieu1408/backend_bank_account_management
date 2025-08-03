package com.klb.account_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenExchangeRequest {
    private String grant_type;
    private String client_id;
    private String client_secret;
    private String scope;
}
