package com.example.demoBe.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RsaKeyGenerator {

    public static void main(String[] args) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // private key
        PrivateKey privateKey = keyPair.getPrivate();
        String privateKeyPem = "-----BEGIN PRIVATE KEY-----\n" +
                Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(privateKey.getEncoded()) +
                "\n-----END PRIVATE KEY-----";
        Files.write(Paths.get("src/main/resources/keys/private_key.pem"), privateKeyPem.getBytes());

        // public key
        PublicKey publicKey = keyPair.getPublic();
        String publicKeyPem = "-----BEGIN PUBLIC KEY-----\n" +
                Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(publicKey.getEncoded()) +
                "\n-----END PUBLIC KEY-----";
        Files.write(Paths.get("src/main/resources/keys/public_key.pem"), publicKeyPem.getBytes());

        System.out.println("RSA key pair generated successfully!");
        System.out.println("Private key saved to: src/main/resources/keys/private_key.pem");
        System.out.println("Public key saved to: src/main/resources/keys/public_key.pem");
    }
}
