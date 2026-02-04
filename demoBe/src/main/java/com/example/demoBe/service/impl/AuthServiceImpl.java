package com.example.demoBe.service.impl;

import com.example.demoBe.dto.AuthResponse;
import com.example.demoBe.dto.LoginRequest;
import com.example.demoBe.dto.RegisterRequest;
import com.example.demoBe.entity.User;
import com.example.demoBe.mapper.UserMapper;
import com.example.demoBe.service.AuthService;
import com.example.demoBe.util.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private com.example.demoBe.service.RefreshTokenService refreshTokenService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Override
    public AuthResponse registerLocal(RegisterRequest req) {
        User existingUser = userMapper.findByEmail(req.getEmail());
        if (existingUser != null) {
            throw new RuntimeException("Email already exists. Please login or use a different email.");
        }

        User user = new User();
        user.setUserId(req.getEmail());
        user.setEmail(req.getEmail());
        user.setPwd(passwordEncoder.encode(req.getPassword()));
        user.setAuthProvider("LOCAL");
        user.setStatus("ACTIVE");
        user.setUserType("USER");
        user.setCreatedBy("SYSTEM");
        user.setCreatedDate(LocalDateTime.now());

        userMapper.insertUser(user);

        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = refreshTokenService.generateRefreshToken(user.getUserUid(), user.getUserId());

        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setUserId(user.getUserId());
        response.setUserUid(user.getUserUid());
        response.setUserType(user.getUserType());
        response.setAuthProvider(user.getAuthProvider());
        return response;
    }

    @Override
    public AuthResponse loginLocal(LoginRequest req) {
        User user = userMapper.findByEmail(req.getEmail());

        if (user == null) {
            throw new RuntimeException("Invalid email or password");
        }

        if (!"LOCAL".equals(user.getAuthProvider())) {
            throw new RuntimeException("Please login using " + user.getAuthProvider());
        }
        if (user.getPwd() == null || !passwordEncoder.matches(req.getPwd(), user.getPwd())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new RuntimeException("Account is " + user.getStatus());
        }

        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = refreshTokenService.generateRefreshToken(user.getUserUid(), user.getUserId());

        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setUserId(user.getUserId());
        response.setUserUid(user.getUserUid());
        response.setUserType(user.getUserType());
        response.setAuthProvider(user.getAuthProvider());
        return response;
    }

    @Override
    public AuthResponse loginGoogle(String idToken) {
        try {
            // Verify Google ID token
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null) {
                throw new RuntimeException("Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            String email = payload.getEmail();

            // Find or create user
            User user = userMapper.findByEmail(email);

            if (user == null) {
                // Create new Google user
                user = new User();
                user.setUserId(email);
                user.setEmail(email);
                user.setPwd(null); // No password for Google users
                user.setAuthProvider("GOOGLE");
                user.setStatus("ACTIVE");
                user.setUserType("USER");
                user.setCreatedBy("SYSTEM");
                user.setCreatedDate(LocalDateTime.now());

                userMapper.insertUser(user);
            } else {
                // User exists - check if it's a Google account or allow linking
                if ("LOCAL".equals(user.getAuthProvider())) {
                    // User registered via LOCAL but now logging in via Google
                    // Option: Update authProvider or reject
                    // Per specification: Allow if same email, just use existing user
                }
            }

            // Generate JWT and return response
            String accessToken = jwtUtil.generateToken(user);
            String refreshToken = refreshTokenService.generateRefreshToken(user.getUserUid(), user.getUserId());

            AuthResponse response = new AuthResponse();
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setTokenType("Bearer");
            response.setUserId(user.getUserId());
            response.setUserUid(user.getUserUid());
            response.setUserType(user.getUserType());
            response.setAuthProvider(user.getAuthProvider());
            return response;

        } catch (Exception e) {
            throw new RuntimeException("Google authentication failed: " + e.getMessage());
        }
    }

    @Override
    public AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = refreshTokenService.generateRefreshToken(user.getUserUid(), user.getUserId());

        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setUserId(user.getUserId());
        response.setUserUid(user.getUserUid());
        response.setUserType(user.getUserType());
        response.setAuthProvider(user.getAuthProvider());
        return response;
    }
}
