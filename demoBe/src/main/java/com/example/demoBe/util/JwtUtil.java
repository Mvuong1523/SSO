package com.example.demoBe.util;

import com.example.demoBe.entity.User;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration; // 90 seconds

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration; // 1 day

    @Autowired
    private RsaKeyGenerator rsaKeyGenerator;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        try {
            // Load RSA keys from resources
            String privateKeyPath = getClass().getClassLoader().getResource("keys/private_key.pem").getPath();
            String publicKeyPath = getClass().getClassLoader().getResource("keys/public_key.pem").getPath();

            this.privateKey = rsaKeyGenerator.loadPrivateKey(privateKeyPath);
            this.publicKey = rsaKeyGenerator.loadPublicKey(publicKeyPath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load RSA keys", e);
        }
    }


    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setSubject(String.valueOf(user.getUserUid()))
                .claim("userId", user.getUserId())
                .claim("userType", user.getUserType())
                .claim("email", user.getEmail())
                .claim("tokenType", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(privateKey, SignatureAlgorithm.RS256) // RSA signing
                .compact();
    }


    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .setSubject(String.valueOf(user.getUserUid()))
                .claim("userId", user.getUserId())
                .claim("tokenType", "refresh")
                .setId(UUID.randomUUID().toString()) // Unique ID for refresh token
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }


    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(publicKey) // Verify with public key
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }


    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }


    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }


    public PublicKey getPublicKey() {
        return this.publicKey;
    }
}
