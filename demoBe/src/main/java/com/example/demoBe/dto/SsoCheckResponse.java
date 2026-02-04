package com.example.demoBe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SsoCheckResponse {
    private boolean authenticated;
    private String userId;
    private Long userUid;
    private String userType;
    private String authProvider;
}
