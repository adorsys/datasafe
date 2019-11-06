package de.adorsys.datasafe.encrypiton.impl.keystore;

import com.google.common.collect.ImmutableMap;
import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyID;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAccess;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyEntry;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;
import de.adorsys.keymanagement.api.Juggler;
import de.adorsys.keymanagement.api.config.keystore.KeyStoreConfig;
import de.adorsys.keymanagement.api.types.KeySetTemplate;
import de.adorsys.keymanagement.api.types.source.KeySet;
import de.adorsys.keymanagement.api.types.template.generated.Encrypting;
import de.adorsys.keymanagement.api.types.template.generated.Secret;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig.DOCUMENT_KEY_ID_PREFIX;
import static de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig.EncryptingKeyCreationCfg;
import static de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig.PATH_KEY_ID_PREFIX;
import static de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig.PATH_KEY_ID_PREFIX_CTR;

@Slf4j
@RuntimeDelegate
public class KeyStoreServiceImpl implements KeyStoreService {

    private final KeyStoreConfig config;
    private final String passwordStoreEncAlgo;
    private final Juggler juggler;

    @Inject
    public KeyStoreServiceImpl(KeyStoreConfig config, Juggler juggler) {
        this.config = config;
        this.passwordStoreEncAlgo = this.config.getPasswordKeysAlgo();
        this.juggler = juggler;
    }

    @Override
    public KeyStore createKeyStore(KeyStoreAuth keyStoreAuth,
                                   KeyCreationConfig config) {

        return createKeyStore(
                keyStoreAuth,
                config,
                ImmutableMap.of(
                        new KeyID(PATH_KEY_ID_PREFIX + UUID.randomUUID().toString()), Optional.empty(),
                        new KeyID(PATH_KEY_ID_PREFIX_CTR + UUID.randomUUID().toString()), Optional.empty(),
                        new KeyID(DOCUMENT_KEY_ID_PREFIX + UUID.randomUUID().toString()), Optional.empty()
                )
        );
    }

    @Override
    public KeyStore createKeyStore(KeyStoreAuth keyStoreAuth,
                                   KeyCreationConfig keyConfig,
                                   Map<KeyID, Optional<SecretKeyEntry>> secretKeys) {

        log.debug("start create keystore ");

        EncryptingKeyCreationCfg encConf = keyConfig.getEncrypting();
        Supplier<char[]> passSupplier = () -> keyStoreAuth.getReadKeyPassword().getValue();
        KeySetTemplate template = KeySetTemplate.builder()
                .generatedEncryptionKeys(Encrypting.with()
                        .algo(encConf.getAlgo())
                        .sigAlgo(encConf.getSigAlgo())
                        .keySize(encConf.getSize())
                        .prefix("ENC")
                        .password(passSupplier)
                        .build()
                        .repeat(keyConfig.getEncKeyNumber())
                )
                .generatedSecretKeys(secretKeys.keySet().stream()
                        .map(it -> Secret.with()
                                .prefix(it.getValue())
                                .password(passSupplier)
                                .build()
                        )
                        .collect(Collectors.toList()))
                .build();
        KeySet keySet = juggler.generateKeys().fromTemplate(template);

        KeyStore ks = juggler.toKeystore().generate(keySet);
        log.debug("finished create keystore ");
        return ks;
    }

    @Override
    @SneakyThrows
    public KeyStore updateKeyStoreReadKeyPassword(KeyStore current,
                                                  KeyStoreAuth currentCredentials,
                                                  KeyStoreAuth newCredentials) {
        Function<String, char[]> keyPass = id -> currentCredentials.getReadKeyPassword().getValue();
        Function<String, char[]> newKeyPass = id -> newCredentials.getReadKeyPassword().getValue();
        KeySet clonedSet = juggler.readKeys()
                .fromKeyStore(current, keyPass)
                .copyToKeySet(newKeyPass);
        return juggler.toKeystore().generate(clonedSet, () -> null);
    }

    @Override
    @SneakyThrows
    public List<PublicKeyIDWithPublicKey> getPublicKeys(KeyStoreAccess keyStoreAccess) {
        log.debug("get public keys");
        List<PublicKeyIDWithPublicKey> result = new ArrayList<>();
        KeyStore keyStore = keyStoreAccess.getKeyStore();
        for (Enumeration<String> keyAliases = keyStore.aliases(); keyAliases.hasMoreElements(); ) {
            final String keyAlias = keyAliases.nextElement();
            X509Certificate cert = (X509Certificate) keyStore.getCertificate(keyAlias);
            if (cert == null) continue; // skip
            boolean[] keyUsage = cert.getKeyUsage();
            // digitalSignature (0), nonRepudiation (1), keyEncipherment (2), dataEncipherment (3),
            // keyAgreement (4), keyCertSign (5), cRLSign (6), encipherOnly (7), decipherOnly (8)
            if (keyUsage[2] || keyUsage[3] || keyUsage[4]) {
                result.add(new PublicKeyIDWithPublicKey(new KeyID(keyAlias), cert.getPublicKey()));
            }
        }
        return result;
    }

    @Override
    @SneakyThrows
    public PrivateKey getPrivateKey(KeyStoreAccess keyStoreAccess, KeyID keyID) {
        ReadKeyPassword readKeyPassword = keyStoreAccess.getKeyStoreAuth().getReadKeyPassword();
        KeyStore keyStore = keyStoreAccess.getKeyStore();
        PrivateKey privateKey;
        privateKey = (PrivateKey) keyStore.getKey(keyID.getValue(), readKeyPassword.getValue());
        return privateKey;
    }

    @Override
    @SneakyThrows
    public SecretKeySpec getSecretKey(KeyStoreAccess keyStoreAccess, KeyID keyID) {
        KeyStore keyStore = keyStoreAccess.getKeyStore();
        char[] password = keyStoreAccess.getKeyStoreAuth().getReadKeyPassword().getValue();
        return (SecretKeySpec) keyStore.getKey(keyID.getValue(), password);
    }

    @Override
    @SneakyThrows
    public void addPasswordBasedSecretKey(KeyStoreAccess keyStoreAccess, String alias, char[] secret) {
        PBEKeySpec pbeKeySpec = new PBEKeySpec(secret);
        SecretKeyFactory keyFac = SecretKeyFactory.getInstance(passwordStoreEncAlgo);
        SecretKey key = keyFac.generateSecret(pbeKeySpec);
        keyStoreAccess.getKeyStore()
                .setKeyEntry(
                        alias,
                        key,
                        keyStoreAccess.getKeyStoreAuth().getReadKeyPassword().getValue(),
                        null
                );
    }

    @Override
    @SneakyThrows
    public void removeKey(KeyStoreAccess keyStoreAccess, String alias) {
        keyStoreAccess.getKeyStore().deleteEntry(alias);
    }

    @Override
    @SneakyThrows
    public byte[] serialize(KeyStore store, ReadStorePassword readStorePassword) {
        return juggler.serializeDeserialize().serialize(store, readStorePassword::getValue);
    }

    @Override
    @SneakyThrows
    public KeyStore deserialize(byte[] payload, ReadStorePassword readStorePassword) {
        return juggler.serializeDeserialize().deserialize(payload, readStorePassword::getValue);
    }
}
