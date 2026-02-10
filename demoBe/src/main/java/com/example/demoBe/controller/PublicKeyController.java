package com.example.demoBe.controller;

import com.example.demoBe.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller to expose Public Key for subdomain JWT verification
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174" }, allowCredentials = "true")
public class PublicKeyController {

    @Autowired
    private JwtUtil jwtUtil;


    @GetMapping("/public-key")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        PublicKey publicKey = jwtUtil.getPublicKey();
        String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());

        Map<String, String> response = new HashMap<>();
        response.put("publicKey", publicKeyBase64);
        response.put("algorithm", "RSA");
        response.put("format", publicKey.getFormat());

        return ResponseEntity.ok(response);
    }
}
