package com.example.demoBe.controller;

import com.example.demoBe.dto.AuthResponse;
import com.example.demoBe.dto.LoginRequest;
import com.example.demoBe.service.AuthService;
import com.example.demoBe.service.RedisService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private RedisService redisService;

    private static final String COOKIE_NAME = "SSO_SESSION";

    // ================== AUTH (LOCAL via API - Optional if Thymeleaf used)
    // ==================

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.loginLocal(request);

        // Set HttpOnly Cookie (Refresh Token) for Global Session
        Cookie cookie = new Cookie(COOKIE_NAME, authResponse.getRefreshToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        response.addCookie(cookie);

        return ResponseEntity.ok(authResponse);
    }

    // ================== SSO / OAUTH ==================

    @GetMapping("/oauth/authorize")
    public ResponseEntity<?> authorize(
            @RequestParam String redirect_uri,
            HttpServletRequest request,
            HttpServletResponse response) {

        // 1. Check Cookie
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (COOKIE_NAME.equals(c.getName())) {
                    refreshToken = c.getValue();
                    break;
                }
            }
        }

        // 2. Validate Session
        if (refreshToken != null && redisService.hasRefreshToken(refreshToken)) {
            // Valid Session -> Generate Code
            Object data = redisService.getRefreshToken(refreshToken);
            Long userUid = Long.valueOf(data.toString());

            // Pass the CURRENT valid RefreshToken to be reused
            String code = authService.generateAuthCode(userUid, refreshToken);

            // Redirect with code
            String separator = redirect_uri.contains("?") ? "&" : "?";
            String targetUrl = redirect_uri + separator + "code=" + code;

            return ResponseEntity.status(302).header("Location", targetUrl).build();
        }

        // 3. Invalid/No Session -> Redirect to React Frontend Login Page (Central)
        String encodedRedirect = URLEncoder.encode(redirect_uri, StandardCharsets.UTF_8);

        // Pointing to Frontend URL (port 5173) instead of Backend URL
        String loginUrl = "http://localhost:5173/login-central?redirect_uri=" + encodedRedirect;

        return ResponseEntity.status(302).header("Location", loginUrl).build();
    }

    @PostMapping("/oauth/token")
    public ResponseEntity<?> exchangeToken(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        if (code == null)
            return ResponseEntity.badRequest().body("Code required");

        AuthResponse response = authService.exchangeCode(code);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/oauth/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null)
            return ResponseEntity.badRequest().body("Refresh Token required");

        try {
            AuthResponse response = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid Refresh Token");
        }
    }

    @PostMapping("/oauth/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> body, HttpServletResponse response) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        // Clear Cookie
        Cookie cookie = new Cookie(COOKIE_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok("Logged out");
    }
}
