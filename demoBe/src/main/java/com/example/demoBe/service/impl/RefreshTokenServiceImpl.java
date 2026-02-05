package com.example.demoBe.service.impl;

import com.example.demoBe.dto.RefreshTokenInfo;
import com.example.demoBe.service.RefreshTokenService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    // Map: Token String -> Info
    private final Map<String, RefreshTokenInfo> tokenInfoMap = new ConcurrentHashMap<>();

    @Override
    public String generateRefreshToken(Long userUid, String userId) {
        // Always generate a NEW unique token when called.
        // Control of "Reuse" is moved to the Controller via Session.
        String newToken = UUID.randomUUID().toString();
        RefreshTokenInfo info = new RefreshTokenInfo(
                newToken,
                userUid,
                userId,
                LocalDateTime.now().plusDays(30), // 30 days
                false);

        tokenInfoMap.put(newToken, info);
        return newToken;
    }

    @Override
    public void saveToken(RefreshTokenInfo info) {
        tokenInfoMap.put(info.getToken(), info);
    }

    @Override
    public RefreshTokenInfo validateToken(String token) {
        RefreshTokenInfo info = tokenInfoMap.get(token);

        if (info == null) {
            System.out.println("Validate: Token not found");
            return null;
        }

        if (info.isRevoked()) {
            System.out.println("Validate: Token is revoked");
            return null;
        }

        if (info.getExpiresAt().isBefore(LocalDateTime.now())) {
            System.out.println("Validate: Token expired");
            tokenInfoMap.remove(token); // Cleanup
            return null;
        }

        return info;
    }

    @Override
    public void revokeToken(String token) {
        RefreshTokenInfo info = tokenInfoMap.get(token);
        if (info != null) {
            info.setRevoked(true);
        }
    }

    @Override
    public void removeToken(String token) {
        tokenInfoMap.remove(token);
    }

    @Override
    public boolean isSessionActive(Long userUid) {
        // This method is less efficient now without index,
        // but for checking "Active Session" we usually rely on Cookie/HttpSession
        // anyway.
        // We can iterate or ignore. Let's iterate for safety if needed,
        // or just return true if any valid token exists.
        return tokenInfoMap.values().stream()
                .anyMatch(info -> info.getUserUid().equals(userUid)
                        && !info.isRevoked()
                        && info.getExpiresAt().isAfter(LocalDateTime.now()));
    }
}
