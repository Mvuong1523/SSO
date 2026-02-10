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
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private com.example.demoBe.util.JwtUtil jwtUtil;

    private static final String COOKIE_NAME = "SSO_SESSION";

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            AuthResponse authResponse = authService.loginLocal(request);

            Cookie refreshCookie = new Cookie("refresh_token", authResponse.getRefreshToken());
            refreshCookie.setHttpOnly(true);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(24*60*60);
            response.addCookie(refreshCookie);
            return  ResponseEntity.ok(authResponse);
        } catch (DisabledException e){
            return ResponseEntity.status(401);
            .body(Map.of("message","Invalid username or password"))
        }
//        AuthResponse authResponse = authService.loginLocal(request);
//
//
//        Cookie refreshCookie = new Cookie("refresh_token", authResponse.getRefreshToken());
//        refreshCookie.setHttpOnly(true);
//        refreshCookie.setPath("/");
//        refreshCookie.setMaxAge(24 * 60 * 60); // 1 day
//        response.addCookie(refreshCookie);
//
//        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest request) {
        try {
            authService.register(request);
            return ResponseEntity.ok(Map.of("message", "Đăng ký thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
        }
    }

    // ================== SSO / OAUTH ==================

    @GetMapping("/oauth/authorize")
    public ResponseEntity<?> authorize(
            @RequestParam String redirect_uri,
            HttpServletRequest request,
            HttpServletResponse response) {

        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("refresh_token".equals(c.getName())) {
                    refreshToken = c.getValue();
                    break;
                }
            }
        }

        if (refreshToken != null && redisService.hasRefreshToken(refreshToken)) {
            Object data = redisService.getRefreshToken(refreshToken);
            Long userUid = Long.valueOf(data.toString());

            AuthResponse authResponse = authService.generateTokenForUser(userUid, refreshToken);

            String separator = redirect_uri.contains("?") ? "&" : "?";
            String targetUrl = redirect_uri + separator 
                + "access_token=" + authResponse.getAccessToken() 
                + "&refresh_token=" + refreshToken;

            return ResponseEntity.status(302).header("Location", targetUrl).build();
        }

        // 3. Invalid/No Session -> Redirect to Login Page
        String encodedRedirect = URLEncoder.encode(redirect_uri, StandardCharsets.UTF_8);
        String loginUrl = "/login?redirect_uri=" + encodedRedirect;

        return ResponseEntity.status(302).header("Location", loginUrl).build();
    }

    @PostMapping("/oauth/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String refreshToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer")){
            refreshToken = authHeader.substring(7);
            }
        if (refreshToken == null){
            return ResponseEntity.badRequest().body("Refresh Token required");
        }

        try {
            AuthResponse response = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid Refresh Token");
        }
    }

    @PostMapping("/oauth/logout")
    public ResponseEntity<?> logout( HttpServletRequest request, HttpServletResponse response) {
        String authHeader = request.getHeader("Authorization");
        String refreshToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer")){
            refreshToken = authHeader.substring(7);
        }
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        
        Cookie refreshCookie = new Cookie("refresh_token", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);
        
        return ResponseEntity.ok("Logged out");
    }


}
