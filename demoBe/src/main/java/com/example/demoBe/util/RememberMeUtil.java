package com.example.demoBe.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.Cipher;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

@Component
public class RememberMeUtil {

    @Autowired
    private RsaKeyGenerator rsaKeyGenerator;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        try {
            String privateKeyPath = getClass().getClassLoader()
                .getResource("keys/remember_private_key.pem").getPath();
            String publicKeyPath = getClass().getClassLoader()
                .getResource("keys/remember_public_key.pem").getPath();

            this.privateKey = rsaKeyGenerator.loadPrivateKey(privateKeyPath);
            this.publicKey = rsaKeyGenerator.loadPublicKey(publicKeyPath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Remember-Me RSA keys", e);
        }
    }

    public String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encrypted = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt remember-me data", e);
        }
    }

    public String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt remember-me data", e);
        }
    }
}
