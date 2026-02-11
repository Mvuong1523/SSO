package com.example.demoBe.controller;

import com.example.demoBe.dto.AuthResponse;
import com.example.demoBe.dto.LoginRequest;
import com.example.demoBe.dto.RegisterRequest;
import com.example.demoBe.service.AuthService;
import com.example.demoBe.service.RedisService;
import com.example.demoBe.util.CookieUtil;
import com.example.demoBe.util.RememberMeUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.security.authentication.DisabledException;

@RestController
@RequestMapping("/api")
public class AuthController {
    @Autowired
    private CookieUtil cookieUtil;

    @Autowired
    private RememberMeUtil rememberMeUtil;

    @Autowired
    private AuthService authService;

    @Autowired
    private RedisService redisService;

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response, HttpServletRequest httpRequest) {
        try {
            AuthResponse authResponse = authService.loginLocal(request);

            Cookie refreshCookie = new Cookie("refresh_token", authResponse.getRefreshToken());
            refreshCookie.setHttpOnly(true);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(24*60*60);
            response.addCookie(refreshCookie);

            if (request.isRememberMe()){
                String data = request.getUsername() + ":" + request.getPassword();
                String encrypted = rememberMeUtil.encrypt(data);
                String existAccounts = cookieUtil.getCookieValue(httpRequest,"remember-me");
                String newAccounts = cookieUtil.addRememberMe(
                        existAccounts,
                        request.getUsername(),
                        encrypted
                );
                
                String encodedAccounts = URLEncoder.encode(newAccounts, StandardCharsets.UTF_8);
                Cookie remember = new Cookie("remember-me", encodedAccounts);
                remember.setMaxAge( 30 * 24 * 60 * 60);
                remember.setPath("/");
                remember.setHttpOnly(true);
                response.addCookie(remember);

                String savedAccounts = cookieUtil.getCookieValue(httpRequest, "savedAccounts");
                savedAccounts = cookieUtil.addAccountToList(savedAccounts, request.getUsername());
                
                Cookie accountsCookie = new Cookie("savedAccounts", savedAccounts);
                accountsCookie.setHttpOnly(false);
                accountsCookie.setPath("/");
                accountsCookie.setMaxAge(30*24*60*60);
                response.addCookie(accountsCookie);

            } else {
                String existingRememberMe = cookieUtil.getCookieValue(httpRequest, "remember-me");
                String existingSavedAccounts = cookieUtil.getCookieValue(httpRequest, "savedAccounts");
                
                if (existingRememberMe != null && !existingRememberMe.isEmpty()) {
                    String token = cookieUtil.getRememberMeToken(existingRememberMe, request.getUsername());
                    
                    if (token != null) {
                        // Tài khoản đã được lưu trước đó -> xóa đi
                        String updatedRememberMe = cookieUtil.removeRememberMeToken(existingRememberMe, request.getUsername());
                        String encodedUpdatedRememberMe = URLEncoder.encode(updatedRememberMe, StandardCharsets.UTF_8);
                        
                        Cookie rememberCookie = new Cookie("remember-me", encodedUpdatedRememberMe);
                        rememberCookie.setMaxAge(30 * 24 * 60 * 60);
                        rememberCookie.setPath("/");
                        rememberCookie.setHttpOnly(true);
                        response.addCookie(rememberCookie);
                        
                        // Xóa khỏi savedAccounts
                        String updatedSavedAccounts = cookieUtil.removeAccountFromList(existingSavedAccounts, request.getUsername());
                        Cookie accountsCookie = new Cookie("savedAccounts", updatedSavedAccounts);
                        accountsCookie.setHttpOnly(false);
                        accountsCookie.setPath("/");
                        accountsCookie.setMaxAge(30 * 24 * 60 * 60);
                        response.addCookie(accountsCookie);
                    }
                }
            }
            return  ResponseEntity.ok(authResponse);
        } catch (DisabledException e){
            return ResponseEntity.status(401).body(Map.of("message","Invalid username or password"));
        }
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
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
