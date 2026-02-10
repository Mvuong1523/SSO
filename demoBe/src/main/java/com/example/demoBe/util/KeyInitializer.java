package com.example.demoBe.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.security.KeyPair;

/**
 * Generate RSA keys on application startup if they don't exist
 */
@Component
public class KeyInitializer implements CommandLineRunner {

    @Autowired
    private RsaKeyGenerator rsaKeyGenerator;

    @Override
    public void run(String... args) throws Exception {
        String resourcePath = getClass().getClassLoader().getResource("").getPath();
        String keysDir = resourcePath + "keys/";
        String privateKeyPath = keysDir + "private_key.pem";
        String publicKeyPath = keysDir + "public_key.pem";

        File dir = new File(keysDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File privateKeyFile = new File(privateKeyPath);
        File publicKeyFile = new File(publicKeyPath);

        if (!privateKeyFile.exists() || !publicKeyFile.exists()) {
            System.out.println("Generating RSA key pair...");
            KeyPair keyPair = rsaKeyGenerator.generateKeyPair();
            rsaKeyGenerator.saveKeyPair(keyPair, privateKeyPath, publicKeyPath);
            System.out.println(" RSA keys generated successfully!");
            System.out.println("   Private Key: " + privateKeyPath);
            System.out.println("   Public Key: " + publicKeyPath);
        } else {
            System.out.println(" RSA keys already exist, skipping generation.");
        }
    }
}
