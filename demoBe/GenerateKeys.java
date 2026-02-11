import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

public class GenerateKeys {
    public static void main(String[] args) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();

        String privateKeyPEM = "-----BEGIN PRIVATE KEY-----\n" +
                Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(pair.getPrivate().getEncoded()) +
                "\n-----END PRIVATE KEY-----\n";

        String publicKeyPEM = "-----BEGIN PUBLIC KEY-----\n" +
                Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(pair.getPublic().getEncoded()) +
                "\n-----END PUBLIC KEY-----\n";

        Files.write(Paths.get("src/main/resources/keys/remember_private_key.pem"), privateKeyPEM.getBytes());
        Files.write(Paths.get("src/main/resources/keys/remember_public_key.pem"), publicKeyPEM.getBytes());

        System.out.println("Keys generated successfully!");
        System.out.println("remember_private_key.pem");
        System.out.println("remember_public_key.pem");
    }
}
