package com.example.demoBe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenInfo {
    private String token;
    private Long userUid;
    private String userId;
    private LocalDateTime expiresAt;
    private boolean revoked;
}
