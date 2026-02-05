package com.example.demoBe.service;

import com.example.demoBe.dto.RefreshTokenInfo;

public interface RefreshTokenService {
    String generateRefreshToken(Long userUid, String userId);

    RefreshTokenInfo validateToken(String token);

    void revokeToken(String token);

    void removeToken(String token);

    void saveToken(RefreshTokenInfo info);

    boolean isSessionActive(Long userUid);
}
