package com.example.demoBe.service;

import com.example.demoBe.dto.AuthResponse;
import com.example.demoBe.dto.LoginRequest;
import com.example.demoBe.entity.User;
import com.example.demoBe.mapper.UserMapper;
import com.example.demoBe.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisService redisService;

    // Local Login (returns User for session creation)
    public AuthResponse loginLocal(LoginRequest request) {
        User user = userMapper.findByUserId(request.getUsername());
        if (user == null) {
            System.out.println("DEBUG: User not found for username: " + request.getUsername());
            throw new RuntimeException("User not found");
        }

        System.out.println("DEBUG: User found! UID: " + user.getUserUid());
        System.out.println("DEBUG: DB Password (Hash): " + user.getPwd());
        System.out.println("DEBUG: Input Password: " + request.getPassword());

        boolean isMatch = passwordEncoder.matches(request.getPassword(), user.getPwd());
        System.out.println("DEBUG: BCrypt Match Result: " + isMatch);
        if (!isMatch && user.getPwd().equals(request.getPassword())) {
            System.out.println("DEBUG: Plain Text Match Result: Valid");
            isMatch = true;
        }

        if (!isMatch) {
            System.out.println("DEBUG: Password verification failed!");
            throw new RuntimeException("Invalid username or password");
        }

        // Generate Tokens
        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = UUID.randomUUID().toString();

        // Save Refresh Token to Redis (Session)
        redisService.saveRefreshToken(refreshToken, user.getUserUid());

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    // Generate Code - Now supports optional Shared Refresh Token
    public String generateAuthCode(Long userUid, String originalRefreshToken) {
        String code = UUID.randomUUID().toString();
        // Save composite value if original token exists
        String value = (originalRefreshToken != null) ? userUid + ":" + originalRefreshToken : String.valueOf(userUid);
        redisService.saveAuthCode(code, value);
        return code;
    }

    public AuthResponse exchangeCode(String code) {
        // Validation
        Object data = redisService.getAuthCode(code);
        if (data == null) {
            throw new RuntimeException("Invalid or expired code");
        }
        // One-time use
        redisService.deleteAuthCode(code);

        String dataStr = data.toString();
        Long userUid;
        String sharedRefreshToken = null;

        // Parse composite value
        if (dataStr.contains(":")) {
            String[] parts = dataStr.split(":");
            userUid = Long.valueOf(parts[0]);
            sharedRefreshToken = parts[1];
        } else {
            userUid = Long.valueOf(dataStr);
        }

        User user = userMapper.findByUserUid(userUid);

        // Generate Tokens
        String accessToken = jwtUtil.generateToken(user);
        String refreshToken;

        if (sharedRefreshToken != null) {
            // REUSE Existing Global Refresh Token (Single Logout Support)
            refreshToken = sharedRefreshToken;
            System.out.println("DEBUG: Reusing Shared Refresh Token: " + refreshToken);
        } else {
            // Fallback: Create new (should rarely happen if flow is correct)
            refreshToken = UUID.randomUUID().toString();
            // Save New Refresh Token to Redis (Session)
            redisService.saveRefreshToken(refreshToken, userUid);
        }

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!redisService.hasRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        // Get User from Token
        Object data = redisService.getRefreshToken(refreshToken);
        Long userUid = Long.valueOf(data.toString());
        User user = userMapper.findByUserUid(userUid);

        // Generate new Access Token
        String newAccessToken = jwtUtil.generateToken(user);

        // Return same Refresh Token (Shared/Long-lived)
        return buildAuthResponse(user, newAccessToken, refreshToken);
    }

    public void logout(String refreshToken) {
        redisService.deleteRefreshToken(refreshToken);
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setUserId(user.getUserId());
        response.setUserUid(user.getUserUid());
        response.setUserType(user.getUserType());
        response.setAuthProvider(user.getAuthProvider());
        return response;
    }
}
