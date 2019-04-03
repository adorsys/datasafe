package de.adorsys.docusafe2.keystore.impl;

import de.adorsys.docusafe2.keystore.api.types.*;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.bouncycastle.cert.X509CertificateHolder;

import javax.crypto.SecretKey;
import javax.security.auth.callback.CallbackHandler;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.*;
import java.security.KeyStore.ProtectionParameter;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.*;
import java.util.stream.Collectors;

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
    public static KeyStore newKeyStore(KeyStoreType keyStoreType) {
        try {
            if (keyStoreType == null) {
                keyStoreType = KeyStoreType.DEFAULT;
            }
            KeyStore ks = KeyStore.getInstance(keyStoreType.getValue());
            ks.load(null, null);
            return ks;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    /**
     * Write this key store into a byte array
     *
     * @param keystore     keystore
     * @param storeId      storeId
     * @param storePassSrc storePassSrc
     * @return key store byte array
     */
    public static byte[] toByteArray(KeyStore keystore, String storeId, CallbackHandler storePassSrc) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            keystore.store(stream, PasswordCallbackUtils.getPassword(storePassSrc, storeId));
            return stream.toByteArray();
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    /**
     * Loads a key store. Given the store bytes, the store type
     *
     * @param in           : the inputStream from which to read the keystore
     * @param storeId      : The store id. This is passed to the callback handler to identify the requested password record.
     * @param storeType    : the type of this key store. f null, the defaut java keystore type is used.
     * @param storePassSrc : the callback handler that retrieves the store password.
     * @return KeyStore
     */
    public static KeyStore loadKeyStore(InputStream in, String storeId, KeyStoreType storeType, CallbackHandler storePassSrc) {
        try {

            // Use default type if blank.
            if (storeType == null) storeType = KeyStoreType.DEFAULT;

            KeyStore ks = KeyStore.getInstance(storeType.getValue());

            ks.load(in, PasswordCallbackUtils.getPassword(storePassSrc, storeId));
            return ks;

        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public static KeyStore loadKeyStore(KeyStoreType keyStoreType, KeyStore.LoadStoreParameter loadStoreParameter) {
        try {

            // Use default type if blank.
            if (keyStoreType == null) {
                keyStoreType = KeyStoreType.DEFAULT;
            }

            KeyStore ks = KeyStore.getInstance(keyStoreType.getValue());

            ks.load(loadStoreParameter);
            return ks;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    /**
     * @param data         : the byte array containing key store data.
     * @param storeId      : The store id. This is passed to the callback handler to identify the requested password record.
     * @param keyStoreType    : the type of this key store. f null, the defaut java keystore type is used.
     * @param storePassSrc : the callback handler that retrieves the store password.
     * @return KeyStore
     */
    public static KeyStore loadKeyStore(byte[] data, String storeId, KeyStoreType keyStoreType, CallbackHandler storePassSrc) {
        return loadKeyStore(new ByteArrayInputStream(data), storeId, keyStoreType, storePassSrc);
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
        } else if (keyEntry instanceof TrustedCertEntry) {
            addToKeyStore(ks, (TrustedCertEntry) keyEntry);
        }
    }

    private static void addToKeyStore(final KeyStore ks, KeyPairEntry keyPairHolder) {
        try {
            List<Certificate> chainList = new ArrayList<>();
            CertificationResult certification = keyPairHolder.getCertification();
            X509CertificateHolder subjectCert = certification != null ? certification.getSubjectCert() : keyPairHolder.getKeyPair().getSubjectCert();
            chainList.add(V3CertificateUtils.getX509JavaCertificate(subjectCert));
            if (certification != null) {
                List<X509CertificateHolder> issuerChain = certification.getIssuerChain();
                for (X509CertificateHolder x509CertificateHolder : issuerChain) {
                    chainList.add(V3CertificateUtils.getX509JavaCertificate(x509CertificateHolder));
                }
            }
            Certificate[] chain = chainList.toArray(new Certificate[chainList.size()]);
            ks.setKeyEntry(keyPairHolder.getAlias(), keyPairHolder.getKeyPair().getKeyPair().getPrivate(),
                    PasswordCallbackUtils.getPassword(keyPairHolder.getPasswordSource(), keyPairHolder.getAlias()), chain);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public static void addToKeyStore(final KeyStore ks, SecretKeyEntry secretKeyData) {
        KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(secretKeyData.getSecretKey());
        ProtectionParameter protParam = getPasswordProtectionParameter(secretKeyData.getPasswordSource(), secretKeyData.getAlias());
        try {
            ks.setEntry(secretKeyData.getAlias(), entry, protParam);
        } catch (KeyStoreException e) {
            // Key store not initialized
            throw new IllegalStateException(e);
        }
    }

    private static ProtectionParameter getPasswordProtectionParameter(CallbackHandler passwordSource, String alias) {
        return new KeyStore.PasswordProtection(PasswordCallbackUtils.getPassword(passwordSource, alias));
    }

    private static void addToKeyStore(final KeyStore ks, TrustedCertEntry trustedCertHolder) {
        try {
            ks.setCertificateEntry(trustedCertHolder.getAlias(), V3CertificateUtils.getX509JavaCertificate(trustedCertHolder.getCertificate()));
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public static List<KeyEntry> loadEntries(KeyStore keyStore, PasswordProvider passwordProvider) {
        List<KeyEntry> keyEntries = new ArrayList<>();
        Enumeration<String> aliases;

        try {
            aliases = keyStore.aliases();
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }

        for (String alias : Collections.list(aliases)) {
            KeyStore.Entry entry;
            try {
                CallbackHandler passwordSource = passwordProvider.providePasswordCallbackHandler(alias);
                entry = keyStore.getEntry(alias, getPasswordProtectionParameter(passwordSource, alias));
                KeyEntry keyEntry = createFromKeyStoreEntry(alias, entry, passwordSource);

                keyEntries.add(keyEntry);
            } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException e) {
                throw new RuntimeException(e);
            }
        }

        return keyEntries;
    }

    public static Map<String, KeyEntry> loadEntryMap(KeyStore keyStore, PasswordProvider passwordProvider) {
        Map<String, KeyEntry> keyEntries = new HashMap<>();
        Enumeration<String> aliases;

        try {
            aliases = keyStore.aliases();
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }

        for (String alias : Collections.list(aliases)) {
            KeyStore.Entry entry;
            try {
                CallbackHandler passwordSource = passwordProvider.providePasswordCallbackHandler(alias);
                entry = keyStore.getEntry(alias, getPasswordProtectionParameter(passwordSource, alias));
                KeyEntry keyEntry = createFromKeyStoreEntry(alias, entry, passwordSource);

                keyEntries.put(alias, keyEntry);
            } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException e) {
                throw new RuntimeException(e);
            }
        }

        return keyEntries;
    }

    private static KeyEntry createFromKeyStoreEntry(String alias, KeyStore.Entry entry, CallbackHandler passwordSource) {
        if (entry instanceof KeyStore.PrivateKeyEntry) {
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) entry;
            return fromPrivateKeyEntry(alias, passwordSource, privateKeyEntry);
        } else if (entry instanceof KeyStore.SecretKeyEntry) {
            KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) entry;
            SecretKey secretKey = secretKeyEntry.getSecretKey();

            return SecretKeyData.builder()
                    .alias(alias)
                    .passwordSource(passwordSource)
                    .secretKey(secretKey)
                    .keyAlgo(secretKey.getAlgorithm())
                    .build();
        } else if (entry instanceof KeyStore.TrustedCertificateEntry) {
            KeyStore.TrustedCertificateEntry trustedCertificateEntry = (KeyStore.TrustedCertificateEntry) entry;

            return TrustedCertData.builder()
                    .alias(alias)
                    .passwordSource(passwordSource)
                    .certificate(toX509CertificateHolder(trustedCertificateEntry.getTrustedCertificate()))
                    .build();
        } else {
            throw new RuntimeException("Unknown type: " + entry.getClass());
        }
    }

    private static KeyPairEntry fromPrivateKeyEntry(String alias, CallbackHandler passwordSource, KeyStore.PrivateKeyEntry privateKeyEntry) {
        KeyPair keyPair = new KeyPair(privateKeyEntry.getCertificate().getPublicKey(), privateKeyEntry.getPrivateKey());

        X509CertificateHolder subjectCert = toX509CertificateHolder(privateKeyEntry.getCertificate());
        SelfSignedKeyPairData keyPairData = new SelfSignedKeyPairData(keyPair, subjectCert);

        CertificationResult certification = new CertificationResult(subjectCert, toX509CertificateHolders(privateKeyEntry.getCertificateChain()));

        return KeyPairData.builder()
                .alias(alias)
                .keyPair(keyPairData)
                .certification(certification)
                .passwordSource(passwordSource)
                .build();
    }

    private static List<X509CertificateHolder> toX509CertificateHolders(Certificate[] certificates) {
        return Arrays.stream(certificates)
                .map(KeyStoreServiceImplBaseFunctions::toX509CertificateHolder)
                .collect(Collectors.toList());
    }

    private static X509CertificateHolder toX509CertificateHolder(Certificate certificate) {
        org.bouncycastle.asn1.x509.Certificate bouncyCastleAsn1Certificate = null;

        try {
            bouncyCastleAsn1Certificate = org.bouncycastle.asn1.x509.Certificate.getInstance(certificate.getEncoded());
        } catch (CertificateEncodingException e) {
            throw new RuntimeException(e);
        }

        return new X509CertificateHolder(bouncyCastleAsn1Certificate);
    }

    public interface PasswordProvider {
        CallbackHandler providePasswordCallbackHandler(String keyAlias);
    }

    public static class PasswordProviderMap implements PasswordProvider {
        private final Map<String, char[]> passwordsForAlias;

        public PasswordProviderMap(Map<String, char[]> passwordsForAlias) {
            this.passwordsForAlias = passwordsForAlias;
        }

        @Override
        public CallbackHandler providePasswordCallbackHandler(String keyAlias) {
            char[] password = passwordsForAlias.get(keyAlias);

            if (password == null) {
                throw new RuntimeException("Password for alias '" + keyAlias + "' not found");
            }

            return new PasswordCallbackHandler(password);
        }
    }

    public static class SimplePasswordProvider implements PasswordProvider {

        private final CallbackHandler callbackHandler;

        public SimplePasswordProvider(char[] password) {
            this.callbackHandler = new PasswordCallbackHandler(password);
        }

        public SimplePasswordProvider(CallbackHandler callbackHandler) {
            this.callbackHandler = callbackHandler;
        }

        @Override
        public CallbackHandler providePasswordCallbackHandler(String keyAlias) {
            return callbackHandler;
        }
    }
}

