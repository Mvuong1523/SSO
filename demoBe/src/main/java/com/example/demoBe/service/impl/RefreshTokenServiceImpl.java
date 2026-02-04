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

    // Map 1: Lưu token đang active của user (UserUid -> Token String)
    // Giúp đảm bảo 1 user chỉ có 1 token active và tìm kiếm cực nhanh O(1)
    private final Map<Long, String> userTokens = new ConcurrentHashMap<>();

    // Map 2: Lưu chi tiết thông tin token (Token String -> Info)
    private final Map<String, RefreshTokenInfo> tokenInfoMap = new ConcurrentHashMap<>();

    @Override
    public String generateRefreshToken(Long userUid, String userId) {
        // 1. Kiểm tra xem user đã có token active chưa
        String existingToken = userTokens.get(userUid);

        if (existingToken != null) {
            RefreshTokenInfo info = tokenInfoMap.get(existingToken);
            // Nếu token còn hạn và chưa bị revoke -> Dùng lại luôn!
            if (info != null && !info.isRevoked() && info.getExpiresAt().isAfter(LocalDateTime.now())) {
                return existingToken; // <--- RETURN TOKEN CŨ
            }
        }

        // 2. Nếu chưa có hoặc đã hết hạn -> Tạo mới
        return createNewToken(userUid, userId);
    }

    private String createNewToken(Long userUid, String userId) {
        // Xóa token cũ nếu có
        String oldToken = userTokens.get(userUid);
        if (oldToken != null) {
            tokenInfoMap.remove(oldToken);
        }

        // Tạo token mới
        String newToken = UUID.randomUUID().toString();
        RefreshTokenInfo info = new RefreshTokenInfo(
                userUid,
                userId,
                LocalDateTime.now().plusDays(30), // 30 ngày
                false);

        // Lưu vào cả 2 map
        userTokens.put(userUid, newToken);
        tokenInfoMap.put(newToken, info);

        return newToken;
    }

    @Override
    public RefreshTokenInfo validateToken(String token) {
        RefreshTokenInfo info = tokenInfoMap.get(token);

        if (info == null) {
            System.out.println("Validate: Token not found: " + token);
            return null; // Không tìm thấy
        }

        if (info.isRevoked()) {
            System.out.println("Validate: Token is revoked: " + token);
            return null; // Đã bị revoke
        }

        if (info.getExpiresAt().isBefore(LocalDateTime.now())) {
            System.out.println("Validate: Token expired");
            // Hết hạn -> Dọn dẹp
            removeToken(token);
            return null;
        }

        // Kiểm tra xem token này có phải là token active của user không
        // (Tránh trường hợp token rác trôi nổi)
        String activeToken = userTokens.get(info.getUserUid());
        if (activeToken == null || !activeToken.equals(token)) {
            System.out.println("Validate: Token content valid but not active for user");
            return null;
        }

        return info;
    }

    @Override
    public void revokeToken(String token) {
        RefreshTokenInfo info = tokenInfoMap.get(token);
        if (info != null) {
            System.out.println("Revoking token found in map for user: " + info.getUserId());
            info.setRevoked(true); // Đánh dấu revoked
            // Không xóa khỏi map để biết là nó đã từng tồn tại nhưng bị cấm
        } else {
            System.out.println("Revoke: Token not found in map: " + token);
        }
    }

    @Override
    public void removeToken(String token) {
        RefreshTokenInfo info = tokenInfoMap.remove(token);
        if (info != null) {
            userTokens.remove(info.getUserUid());
        }
    }

    @Override
    public boolean isSessionActive(Long userUid) {
        String token = userTokens.get(userUid);
        if (token == null) {
            return false;
        }
        RefreshTokenInfo info = tokenInfoMap.get(token);
        return info != null && !info.isRevoked() && info.getExpiresAt().isAfter(LocalDateTime.now());
    }
}
