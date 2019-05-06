package de.adorsys.datasafe.business.impl.encryption.pathencryption;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public interface PathEncryptionConfig {

    Cipher encryptionCipher(SecretKey secretKey);
    Cipher decryptionCipher(SecretKey secretKey);
    String byteSerializer(byte[] bytes);
    byte[] byteDeserializer(String input);
}
