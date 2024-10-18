package de.adorsys.datasafe.business.impl.service;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAccess;
import de.adorsys.datasafe.encrypiton.impl.keystore.KeyStoreServiceImpl;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

public class DataConverter {
    private final KeyStoreServiceImpl keyStoreService;

    public DataConverter(KeyStoreServiceImpl keyStoreService) {
        this.keyStoreService = keyStoreService;
    }

    public String convertData(String encryptedData, KeyStoreAccess user) throws Exception {
        // Retrieve the RSA private key for decryption
        PrivateKey rsaPrivateKey = keyStoreService.getPrivateKey(user, "RSA");

        // Decrypt the data using RSA private key
        byte[] decryptedData = decryptWithRSA(encryptedData, rsaPrivateKey);

        // Generate or retrieve the EC public key for encryption
        PublicKey ecPublicKey = getECPublicKey(user);

        // Encrypt the decrypted data using EC public key

        return encryptWithEC(decryptedData, ecPublicKey);
    }

    public byte[] decryptWithRSA(String encryptedData, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(Base64.getDecoder().decode(encryptedData));
    }

    public String encryptWithEC(byte[] data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("ECIES"); // Assuming ECIES is supported
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(data);
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public PublicKey getECPublicKey(KeyStoreAccess user) throws Exception {
        // Generate EC key pair if not already present
        KeyPair ecKeyPair = generateECKeyPair(user);
        return ecKeyPair.getPublic();
    }

    public KeyPair generateECKeyPair(KeyStoreAccess user) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
        keyPairGenerator.initialize(ecSpec);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // Store the EC key pair in the key store
        keyStoreService.addKeyPair(user, keyPair.getPrivate(), keyPair.getPublic());

        return keyPair;
    }

    public void deleteRSAKeyPair(UserIDAuth user) {
        // Implement the logic to delete the RSA key pair from the keystore
        keyStoreService.deleteKeyPair(user, "RSA");
        System.out.println("Deleted RSA key pair for user: " + user.getUserID());
    }
}
