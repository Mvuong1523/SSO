package com.example.demoBe.controller;

import com.example.demoBe.dto.AuthResponse;
import com.example.demoBe.dto.GoogleLoginRequest;
import com.example.demoBe.dto.LoginRequest;
import com.example.demoBe.dto.RefreshTokenRequest;
import com.example.demoBe.dto.RegisterRequest;
import com.example.demoBe.dto.SsoCheckResponse;
import com.example.demoBe.dto.RefreshTokenInfo;
import com.example.demoBe.entity.User;
import com.example.demoBe.mapper.UserMapper;
import com.example.demoBe.service.AuthService;
import com.example.demoBe.service.RefreshTokenService;
import com.example.demoBe.util.JwtUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    private static final String SESSION_USER_KEY = "SSO_USER_UID";
    private static final String SESSION_REFRESH_TOKEN_KEY = "SSO_REFRESH_TOKEN";

    // ==================== Local Auth ====================

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        authService.registerLocal(request);
        return ResponseEntity.ok(Map.of("message", "Registration successful. Please login."));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request, HttpSession session) {
        AuthResponse response = authService.loginLocal(request);
        session.setAttribute(SESSION_USER_KEY, response.getUserUid());
        System.out.println("LOGIN SUCCESS: Session ID = " + session.getId());
        System.out.println("LOGIN SUCCESS: Set User UID = " + response.getUserUid());

        // Generate and Save Session-Bound Refresh Token
        String refreshToken = refreshTokenService.generateRefreshToken(response.getUserUid(), response.getUserId());
        session.setAttribute(SESSION_REFRESH_TOKEN_KEY, refreshToken);
        response.setRefreshToken(refreshToken);

        return ResponseEntity.ok(response);
    }

    // ==================== OAuth ====================

    @PostMapping("/oauth/google")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody GoogleLoginRequest request, HttpSession session) {
        AuthResponse response = authService.loginGoogle(request.getIdToken());
        session.setAttribute(SESSION_USER_KEY, response.getUserUid());

        // Generate and Save Session-Bound Refresh Token
        String refreshToken = refreshTokenService.generateRefreshToken(response.getUserUid(), response.getUserId());
        session.setAttribute(SESSION_REFRESH_TOKEN_KEY, refreshToken);
        response.setRefreshToken(refreshToken);

        return ResponseEntity.ok(response);
    }

    // ==================== SSO ====================

    @GetMapping("/sso/check")
    public ResponseEntity<SsoCheckResponse> checkSession(HttpSession session) {
        Long userUid = (Long) session.getAttribute(SESSION_USER_KEY);

        if (userUid == null) {
            return ResponseEntity.ok(new SsoCheckResponse(false, null, null, null, null));
        }

        User user = userMapper.findByUserUid(userUid);
        if (user == null) {
            session.invalidate();
            return ResponseEntity.ok(new SsoCheckResponse(false, null, null, null, null));
        }

        return ResponseEntity.ok(new SsoCheckResponse(
                true,
                user.getUserId(),
                user.getUserUid(),
                user.getUserType(),
                user.getAuthProvider()));
    }

    // Inner class to hold data associated with an Auth Code
    private static class AuthCodeData {
        Long userUid;
        String refreshToken;

        public AuthCodeData(Long userUid, String refreshToken) {
            this.userUid = userUid;
            this.refreshToken = refreshToken;
        }
    }

    private static final Map<String, AuthCodeData> authCodes = new java.util.concurrent.ConcurrentHashMap<>();

    // ==================== SSO (Redirect Flow) ====================

    @GetMapping("/sso/authorize")
    public ResponseEntity<?> authorize(
            @RequestParam String redirectUrl,
            HttpSession session) {

        Long userUid = (Long) session.getAttribute(SESSION_USER_KEY);

        System.out.println("SSO AUTHORIZE: Session ID = " + session.getId());
        System.out.println("SSO AUTHORIZE: Request URL = " + redirectUrl);
        System.out.println("SSO AUTHORIZE: Session User UID = " + userUid);

        if (userUid == null) {
            System.out.println("SSO AUTHORIZE: FAILURE - User not found in session.");
            // "True SSO": If not logged in, redirect back to the Client's Login Page
            // (which is usually the start_sso logic, but we need to tell it to STOP
            // looping)
            // We append error=login_required so the Client knows to show the Form.

            // NOTE: Ideally we should validate redirectUrl against a whitelist for
            // security.

            String separator = redirectUrl.contains("?") ? "&" : "?";
            String fullRedirect = redirectUrl + separator + "error=login_required";

            return ResponseEntity.status(302)
                    .header("Location", fullRedirect)
                    .build();
        }

        // Get Session-Bound Refresh Token
        String refreshToken = (String) session.getAttribute(SESSION_REFRESH_TOKEN_KEY);

        // If missing (legacy session?), generate one
        if (refreshToken == null) {
            User user = userMapper.findByUserUid(userUid);
            if (user != null) {
                refreshToken = refreshTokenService.generateRefreshToken(userUid, user.getUserId());
                session.setAttribute(SESSION_REFRESH_TOKEN_KEY, refreshToken);
            }
        }

        // Generate Code mapping to User + Token
        String code = java.util.UUID.randomUUID().toString();
        // Use proper data structure for map
        authCodes.put(code, new AuthCodeData(userUid, refreshToken));

        String separator = redirectUrl.contains("?") ? "&" : "?";
        return ResponseEntity.status(302)
                .header("Location", redirectUrl + separator + "code=" + code)
                .build();
    }

    @PostMapping("/sso/exchange")
    public ResponseEntity<?> exchangeToken(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        if (code == null || !authCodes.containsKey(code)) {
            return ResponseEntity.status(400).body(Map.of("message", "Invalid or expired code"));
        }

        AuthCodeData data = authCodes.remove(code); // One-time use
        Long userUid = data.userUid;
        String sessionRefreshToken = data.refreshToken;

        User user = userMapper.findByUserUid(userUid);

        if (user == null) {
            return ResponseEntity.status(400).body(Map.of("message", "User not found"));
        }

        String accessToken = jwtUtil.generateToken(user);

        // Return the Session-Bound Refresh Token!
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(sessionRefreshToken);
        response.setTokenType("Bearer");
        response.setUserId(user.getUserId());
        response.setUserUid(user.getUserUid());
        response.setUserType(user.getUserType());
        response.setAuthProvider(user.getAuthProvider());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        System.out.println("Receive refresh request: " + refreshToken);

        RefreshTokenInfo tokenInfo = refreshTokenService.validateToken(refreshToken);
        if (tokenInfo == null) {
            System.out.println("Refresh failed: Token invalid or revoked");
            return ResponseEntity.status(401).body(Map.of("message", "Invalid or expired refresh token"));
        }

        User user = userMapper.findByUserUid(tokenInfo.getUserUid());
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "User not found"));
        }

        String newAccessToken = jwtUtil.generateToken(user);

        AuthResponse response = new AuthResponse();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setUserId(user.getUserId());
        response.setUserUid(user.getUserUid());
        response.setUserType(user.getUserType());
        response.setAuthProvider(user.getAuthProvider());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/sso/logout")
    public ResponseEntity<?> logout(@RequestBody(required = false) RefreshTokenRequest request, HttpSession session) {
        session.invalidate();

        if (request != null && request.getRefreshToken() != null) {
            System.out.println("Revoking token: " + request.getRefreshToken());
            refreshTokenService.revokeToken(request.getRefreshToken());
        } else {
            System.out.println("Logout called but no refresh token provided");
        }

        return ResponseEntity.ok().body(Map.of("message", "Logged out successfully"));
    }

    // ==================== Profile ====================

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        User user = userMapper.findByUserId(userId);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        }

        return ResponseEntity.ok(Map.of(
                "userId", user.getUserId(),
                "userUid", user.getUserUid(),
                "userType", user.getUserType(),
                "authProvider", user.getAuthProvider()));
    }

    // ==================== JWKS (Public Key) ====================

    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<?> getJwks() {
        RSAPublicKey publicKey = (RSAPublicKey) jwtUtil.getPublicKey();

        String n = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(publicKey.getModulus().toByteArray());
        String e = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(publicKey.getPublicExponent().toByteArray());

        Map<String, Object> key = Map.of(
                "kty", "RSA",
                "alg", "RS256",
                "use", "sig",
                "n", n,
                "e", e);

        return ResponseEntity.ok(Map.of("keys", new Object[] { key }));
    }
}
