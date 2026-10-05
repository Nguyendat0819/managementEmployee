package com.example.employee_service.feature.model.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TokenResponse {
    String accessToken;
    String refreshToken;
    String tokenType;
    long expiresIn;
}
