package com.example.demoBe.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private String userId;
    private Long userUid;
    private String userType;
    private String authProvider;
}
