package de.adorsys.docusafe2.business.impl.keystore;

import com.nimbusds.jose.jwk.*;
import de.adorsys.common.exceptions.BaseExceptionHandler;
import de.adorsys.common.utils.HexUtil;
import de.adorsys.docusafe2.business.api.keystore.KeyStoreService;
import de.adorsys.docusafe2.business.api.keystore.exceptions.SymmetricEncryptionException;
import de.adorsys.docusafe2.business.api.keystore.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class KeyStoreServiceImpl implements KeyStoreService {
    private final static Logger LOGGER = LoggerFactory.getLogger(KeyStoreServiceImpl.class);

    /**
     *
     */
    @Override
    public KeyStore createKeyStore(KeyStoreAuth keyStoreAuth,
                                   KeyStoreType keyStoreType,
                                   KeyStoreCreationConfig config) {
        try {
            LOGGER.debug("start create keystore ");
            if (config == null) {
                config = new KeyStoreCreationConfig(5, 5, 5);
            }
            // TODO, hier also statt der StoreID nun das
            String serverKeyPairAliasPrefix = HexUtil.convertBytesToHexString(UUID.randomUUID().toString().getBytes());
            LOGGER.debug("keystoreid = " + serverKeyPairAliasPrefix);
            {
                String realKeyStoreId = new String(HexUtil.convertHexStringToBytes(serverKeyPairAliasPrefix));
                LOGGER.debug("meaning of keystoreid = " + realKeyStoreId);
            }
            KeyStoreGenerator keyStoreGenerator = new KeyStoreGenerator(
                    config,
                    keyStoreType,
                    serverKeyPairAliasPrefix,
                    keyStoreAuth.getReadKeyPassword());
            KeyStore userKeyStore = keyStoreGenerator.generate();
            LOGGER.debug("finished create keystore ");
            return userKeyStore;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public KeySourceAndKeyID getKeySourceAndKeyIDForPublicKey(KeyStoreAccess keyStoreAccess) {
        LOGGER.debug("getKeySourceAndKeyIDForPublicKey ");
        JWKSet exportKeys = load(keyStoreAccess.getKeyStore(), null);
        List<JWK> encKeys = selectEncKeys(exportKeys);
        JWK randomKey = JwkExport.randomKey(encKeys);
        KeyID keyID = new KeyID(randomKey.getKeyID());
        KeySource keySource = new KeyStoreBasedPublicKeySourceImpl(exportKeys);
        return new KeySourceAndKeyID(keySource, keyID);
    }

    @Override
    public PublicKeyJWK getPublicKeyJWK(KeyStoreAccess keyStoreAccess) {
        LOGGER.debug("getPublicKeyJWK ");
        JWKSet exportKeys = load(keyStoreAccess.getKeyStore(), null);
        List<JWK> encKeys = selectEncKeys(exportKeys);
        return new PublicKeyJWK(JwkExport.randomKey(encKeys));
    }

    @Override
    public KeySource getKeySourceForPrivateKey(KeyStoreAccess keyStoreAccess) {
        LOGGER.debug("get keysource for private key of");
        return new KeyStoreBasedPrivateKeySourceImpl(keyStoreAccess.getKeyStore(), keyStoreAccess.getKeyStoreAuth().getReadKeyPassword());
    }

    @Override
    public KeySourceAndKeyID getKeySourceAndKeyIDForSecretKey(KeyStoreAccess keyStoreAccess) {
        LOGGER.debug("get keysource for secret key of ");
        KeySource keySource = new KeyStoreBasedSecretKeySourceImpl(keyStoreAccess.getKeyStore(), new PasswordCallbackHandler(keyStoreAccess.getKeyStoreAuth().getReadKeyPassword().getValue().toCharArray()));
        return new KeySourceAndKeyID(keySource, getRandomSecretKeyIDWithKey(keyStoreAccess).getKeyID());

    }

    @Override
    public SecretKeyIDWithKey getRandomSecretKeyIDWithKey(KeyStoreAccess keyStoreAccess) {
        // Choose a random secret key with its id
        JWKSet jwkSet = JwkExport.exportKeys(keyStoreAccess.getKeyStore(), new PasswordCallbackHandler(keyStoreAccess.getKeyStoreAuth().getReadKeyPassword().getValue().toCharArray()));
        if (jwkSet.getKeys().isEmpty()) {
            throw new SymmetricEncryptionException("did not find any keys in keystore with id: ");
        }
        ServerKeyMap serverKeyMap = new ServerKeyMap(jwkSet);
        KeyAndJwk randomSecretKey;
        try {
            randomSecretKey = serverKeyMap.randomSecretKey();
        } catch (IndexOutOfBoundsException b) {
            throw new SymmetricEncryptionException("did not find any secret keys in keystore with id: ");
        }
        KeyID keyID = new KeyID(randomSecretKey.jwk.getKeyID());
        return new SecretKeyIDWithKey(new KeyID(keyID.getValue()), (SecretKey) randomSecretKey.key);
    }



    private List<JWK> selectEncKeys(JWKSet exportKeys) {
        JWKMatcher signKeys = (new JWKMatcher.Builder()).keyUse(KeyUse.ENCRYPTION).build();
        return (new JWKSelector(signKeys)).select(exportKeys);
    }


    private JWKSet load(final KeyStore keyStore, final PasswordLookup pwLookup) {
        try {

            List<JWK> jwks = new LinkedList<>();

            // Load RSA and EC keys
            for (Enumeration<String> keyAliases = keyStore.aliases(); keyAliases.hasMoreElements(); ) {

                final String keyAlias = keyAliases.nextElement();
                final char[] keyPassword = pwLookup == null ? "".toCharArray() : pwLookup.lookupPassword(keyAlias);

                Certificate cert = keyStore.getCertificate(keyAlias);
                if (cert == null) {
                    continue; // skip
                }

                Certificate[] certs = new Certificate[]{cert};
                if (cert.getPublicKey() instanceof RSAPublicKey) {
                    List<X509Certificate> convertedCert = V3CertificateUtils.convert(certs);
                    RSAKey rsaJWK = RSAKey.parse(convertedCert.get(0));

                    // Let keyID=alias
                    // Converting from a certificate, the id is set as the thumbprint of the certificate.
                    rsaJWK = new RSAKey.Builder(rsaJWK).keyID(keyAlias).keyStore(keyStore).build();
                    jwks.add(rsaJWK);

                } else if (cert.getPublicKey() instanceof ECPublicKey) {
                    List<X509Certificate> convertedCert = V3CertificateUtils.convert(certs);
                    ECKey ecJWK = ECKey.parse(convertedCert.get(0));

                    // Let keyID=alias
                    // Converting from a certificate, the id is set as the thumbprint of the certificate.
                    ecJWK = new ECKey.Builder(ecJWK).keyID(keyAlias).keyStore(keyStore).build();
                    jwks.add(ecJWK);
                } else {
                    continue;
                }
            }
            JWKSet jwkSet = new JWKSet(jwks);
            if (jwkSet.getKeys().isEmpty()) {
                //throw new AsymmetricEncryptionException("did not find any public keys in keystore ");
            }
            return jwkSet;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

}
