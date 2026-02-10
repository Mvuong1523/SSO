package com.example.demoBe.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class RsaKeyGenerator {

    /**
     * Generate RSA key pair (2048 bits)
     */
    public KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    /**
     * Save key pair to PEM files
     */
    public void saveKeyPair(KeyPair keyPair, String privateKeyPath, String publicKeyPath) throws IOException {
        // Save private key
        PrivateKey privateKey = keyPair.getPrivate();
        String privateKeyPEM = convertToPEM(privateKey.getEncoded(), "PRIVATE KEY");
        Files.write(Paths.get(privateKeyPath), privateKeyPEM.getBytes());

        // Save public key
        PublicKey publicKey = keyPair.getPublic();
        String publicKeyPEM = convertToPEM(publicKey.getEncoded(), "PUBLIC KEY");
        Files.write(Paths.get(publicKeyPath), publicKeyPEM.getBytes());
    }

    /**
     * Convert byte array to PEM format
     */
    private String convertToPEM(byte[] keyBytes, String keyType) {
        String base64 = Base64.getEncoder().encodeToString(keyBytes);
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN ").append(keyType).append("-----\n");

        // Split into 64-character lines
        int index = 0;
        while (index < base64.length()) {
            pem.append(base64, index, Math.min(index + 64, base64.length())).append("\n");
            index += 64;
        }

        pem.append("-----END ").append(keyType).append("-----\n");
        return pem.toString();
    }

    /**
     * Load private key from PEM file
     */
    /**
     * Load private key from PEM file (Classpath or File System)
     */
    public PrivateKey loadPrivateKey(String filePath) throws Exception {
        String key;
        try {
            // Try loading from classpath
            ClassPathResource resource = new ClassPathResource(filePath);
            if (resource.exists()) {
                key = new String(resource.getInputStream().readAllBytes());
            } else {
                // Fallback to file system
                key = new String(Files.readAllBytes(Paths.get(sanitizePath(filePath))));
            }
        } catch (Exception e) {
            // If ClassPathResource fails (e.g. invalid path format for it), try file system
            // directly
            key = new String(Files.readAllBytes(Paths.get(sanitizePath(filePath))));
        }

        String privateKeyPEM = key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * Load public key from PEM file
     */
    /**
     * Load public key from PEM file (Classpath or File System)
     */
    public PublicKey loadPublicKey(String filePath) throws Exception {
        String key;
        try {
            ClassPathResource resource = new ClassPathResource(filePath);
            if (resource.exists()) {
                key = new String(resource.getInputStream().readAllBytes());
            } else {
                key = new String(Files.readAllBytes(Paths.get(sanitizePath(filePath))));
            }
        } catch (Exception e) {
            key = new String(Files.readAllBytes(Paths.get(sanitizePath(filePath))));
        }

        String publicKeyPEM = key
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return keyFactory.generatePublic(keySpec);
    }

    private String sanitizePath(String path) {
        // Fix Windows path issue: /D:/... -> D:/...
        if (System.getProperty("os.name").toLowerCase().contains("win") && path.startsWith("/")
                && path.indexOf(":") == 2) {
            return path.substring(1);
        }
        return path;
    }
}
