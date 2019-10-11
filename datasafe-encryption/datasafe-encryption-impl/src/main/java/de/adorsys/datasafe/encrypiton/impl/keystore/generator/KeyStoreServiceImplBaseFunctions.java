package de.adorsys.datasafe.encrypiton.impl.keystore.generator;

import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyEntry;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadStorePassword;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyEntry;
import de.adorsys.datasafe.encrypiton.impl.keystore.KeyStoreCreationConfig;
import de.adorsys.datasafe.encrypiton.impl.keystore.types.KeyPairEntry;
import lombok.SneakyThrows;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.crypto.util.PBKDF2Config;
import org.bouncycastle.jcajce.BCFKSLoadStoreParameter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStore.ProtectionParameter;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Key store manipulation routines.
 *
 * @author fpo
 */
public class KeyStoreServiceImplBaseFunctions {

    private KeyStoreServiceImplBaseFunctions() {
        throw new IllegalStateException("Not supported");
    }

    /**
     * Create an initializes a new key store. The key store is not yet password protected.
     *
     * @param keyStoreConfig storeType
     * @return KeyStore keyStore
     */
    @SneakyThrows
    public static KeyStore newKeyStore(KeyStoreCreationConfig keyStoreConfig) {
        if (keyStoreConfig == null) keyStoreConfig = KeyStoreCreationConfig.DEFAULT;
        KeyStore ks = KeyStore.getInstance(keyStoreConfig.getKeyStoreType());
        if ("BCFKS".equals(keyStoreConfig.getKeyStoreType())) {
            AlgorithmIdentifier prf = (AlgorithmIdentifier) PBKDF2Config.class.getDeclaredField(keyStoreConfig.getStorePBKDFConfig()).get(PBKDF2Config.class);

            ks.load(new BCFKSLoadStoreParameter.Builder()
                    .withStoreEncryptionAlgorithm(BCFKSLoadStoreParameter.EncryptionAlgorithm.valueOf(keyStoreConfig.getStoreEncryptionAlgorithm()))
                    .withStorePBKDFConfig(new PBKDF2Config.Builder().withPRF(prf).build())
                    .withStoreMacAlgorithm(BCFKSLoadStoreParameter.MacAlgorithm.valueOf(keyStoreConfig.getStoreMacAlgorithm()))
                    .build()
            );
        } else {
            ks.load(null, null);
        }
        return ks;
    }

    /**
     * Write this key store into a byte array
     *
     * @param keystore keystore
     * @return key store byte array
     */
    @SneakyThrows
    public static byte[] toByteArray(KeyStore keystore, String storeId, ReadStorePassword readStorePassword) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        keystore.store(stream, readStorePassword.getValue().toCharArray());
        return stream.toByteArray();
    }

    /**
     * Loads a key store. Given the store bytes, the store type
     *
     * @param in        : the inputStream location which to read the keystore
     * @param keyStoreConfig : the type of this key store. f null, the defaut java keystore type is used.
     * @return KeyStore
     */
    @SneakyThrows
    public static KeyStore loadKeyStore(InputStream in, String storeId,
                                        KeyStoreCreationConfig keyStoreConfig,
                                        ReadStorePassword readStorePassword) {
        // Use default if blank.
        if (keyStoreConfig == null) keyStoreConfig = KeyStoreCreationConfig.DEFAULT;

        KeyStore ks = KeyStore.getInstance(keyStoreConfig.getKeyStoreType());

        ks.load(in, readStorePassword.getValue().toCharArray());
        return ks;
    }

    /**
     * @param data         : the byte array containing key store data.
     * @param keyStoreCreationConfig : the type of this key store. f null, the defaut java keystore type is used.
     * @return KeyStore
     */
    public static KeyStore loadKeyStore(byte[] data, String storeId,
                                        KeyStoreCreationConfig keyStoreCreationConfig,
                                        ReadStorePassword readStorePassword) {
        return loadKeyStore(new ByteArrayInputStream(data), storeId, keyStoreCreationConfig, readStorePassword);
    }

    /**
     * Put the given entries into a key store. The key store must have been initialized before.
     *
     * @param ks         ks
     * @param keyEntries keyEntries
     */
    public static void fillKeyStore(final KeyStore ks, Collection<KeyEntry> keyEntries) {
        for (KeyEntry keyEntry : keyEntries) {
            addToKeyStore(ks, keyEntry);
        }
    }

    /**
     * Put the given entry into a key store. The key store must have been initialized before.
     *
     * @param ks       ks
     * @param keyEntry keyEntry to be added
     */
    public static void addToKeyStore(final KeyStore ks, KeyEntry keyEntry) {
        if (keyEntry instanceof KeyPairEntry) {
            addToKeyStore(ks, (KeyPairEntry) keyEntry);
        } else if (keyEntry instanceof SecretKeyEntry) {
            addToKeyStore(ks, (SecretKeyEntry) keyEntry);
        }
    }

    @SneakyThrows
    private static void addToKeyStore(final KeyStore ks, KeyPairEntry keyPairHolder) {
        List<Certificate> chainList = new ArrayList<>();
        X509CertificateHolder subjectCert = keyPairHolder.getKeyPair().getSubjectCert();
        chainList.add(V3CertificateUtils.getX509JavaCertificate(subjectCert));
        Certificate[] chain = chainList.toArray(new Certificate[chainList.size()]);
        ks.setKeyEntry(keyPairHolder.getAlias(), keyPairHolder.getKeyPair().getKeyPair().getPrivate(),
                keyPairHolder.getReadKeyPassword().getValue().toCharArray(), chain);
    }

    @SneakyThrows
    public static void addToKeyStore(final KeyStore ks, SecretKeyEntry secretKeyData) {
        KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(secretKeyData.getSecretKey());
        ProtectionParameter protParam = getPasswordProtectionParameter(secretKeyData.getReadKeyPassword());
        ks.setEntry(secretKeyData.getAlias(), entry, protParam);
    }

    private static ProtectionParameter getPasswordProtectionParameter(ReadKeyPassword readKeyPassword) {
        return new KeyStore.PasswordProtection(readKeyPassword.getValue().toCharArray());
    }
}

