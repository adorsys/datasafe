package de.adorsys.datasafe.encrypiton.impl.keystore.generator;

import de.adorsys.datasafe.encrypiton.api.types.keystore.*;
import de.adorsys.datasafe.encrypiton.impl.keystore.types.KeyPairEntry;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;
import lombok.SneakyThrows;
import org.bouncycastle.cert.X509CertificateHolder;

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
     * @param keyStoreType storeType
     * @return KeyStore keyStore
     */
    @SneakyThrows
    public static KeyStore newKeyStore(KeyStoreType keyStoreType) {
        KeyStore ks = KeyStore.getInstance(keyStoreType.getValue());
        ks.load(null, null);
        return ks;
    }

    /**
     * Write this key store into a byte array
     *
     * @param keystore keystore
     * @param storeId  storeId
     * @return key store byte array
     */
    @SneakyThrows
    public static byte[] toByteArray(KeyStore keystore, String storeId, ReadStorePassword readStorePassword) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        keystore.store(stream, readStorePassword.getValue());
        return stream.toByteArray();
    }

    /**
     * Loads a key store. Given the store bytes, the store type
     *
     * @param in        : the inputStream location which to read the keystore
     * @param storeId   : The store id. This is passed to the callback handler to identify the requested password record.
     * @param storeType : the type of this key store. f null, the defaut java keystore type is used.
     * @return KeyStore
     */
    @SneakyThrows
    public static KeyStore loadKeyStore(InputStream in, String storeId, KeyStoreType storeType, ReadStorePassword readStorePassword) {
        // Use default type if blank.
        if (storeType == null) storeType = KeyStoreType.DEFAULT;

        KeyStore ks = KeyStore.getInstance(storeType.getValue());

        ks.load(in, readStorePassword.getValue());
        return ks;
    }

    /**
     * @param data         : the byte array containing key store data.
     * @param storeId      : The store id. This is passed to the callback handler to identify the requested password record.
     * @param keyStoreType : the type of this key store. f null, the defaut java keystore type is used.
     * @return KeyStore
     */
    public static KeyStore loadKeyStore(byte[] data, String storeId, KeyStoreType keyStoreType, ReadStorePassword readStorePassword) {
        return loadKeyStore(new ByteArrayInputStream(data), storeId, keyStoreType, readStorePassword);
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
                keyPairHolder.getReadKeyPassword().getValue(), chain);
    }

    @SneakyThrows
    public static void addToKeyStore(final KeyStore ks, SecretKeyEntry secretKeyData) {
        KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(secretKeyData.getSecretKey());
        ProtectionParameter protParam = getPasswordProtectionParameter(secretKeyData.getReadKeyPassword());
        ks.setEntry(secretKeyData.getAlias(), entry, protParam);
    }

    private static ProtectionParameter getPasswordProtectionParameter(ReadKeyPassword readKeyPassword) {
        return new KeyStore.PasswordProtection(readKeyPassword.getValue());
    }
}

