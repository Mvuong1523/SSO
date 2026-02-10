package com.example.demoSubDmain.service.impl;

import com.example.demoSubDmain.service.PublicKeyService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

@Service
public class PublicKeyServiceImpl implements PublicKeyService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${auth.server.url}")
    private String authServerUrl;

    @Value("${auth.server.public-key-endpoint}")
    private String publicKeyEndpoint;

    // --- CACHE ---
    private PublicKey cachedPublicKey;

    @PostConstruct
    public void init() {
        refreshPublicKey();
    }

    @Override
    public PublicKey getPublicKey() {
        if (cachedPublicKey == null) {
            refreshPublicKey();
        }
        return cachedPublicKey;
    }

    private void refreshPublicKey() {
        try {
            String url = authServerUrl + publicKeyEndpoint;
            // Call Auth Server
            Map<String, String> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("publicKey")) {
                String publicKeyBase64 = response.get("publicKey");

                // Decode Base64
                byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);

                // Reconstruct RSA Public Key
                X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                this.cachedPublicKey = keyFactory.generatePublic(spec);

                System.out.println(" Public Key loaded successfully from Auth Server!");
            }
        } catch (Exception e) {
            System.err.println(" Failed to load Public Key from Auth Server: " + e.getMessage());
            // Retry logic or Fallback could be added here
        }
    }
}
