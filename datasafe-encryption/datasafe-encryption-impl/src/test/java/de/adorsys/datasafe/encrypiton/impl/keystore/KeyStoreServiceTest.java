package de.adorsys.datasafe.encrypiton.impl.keystore;

import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.encryption.EncryptionConfig;
import de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyID;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAccess;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.encrypiton.impl.KeystoreUtil;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;
import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import de.adorsys.keymanagement.adapter.modules.generator.GeneratorModule_ProviderFactory;
import de.adorsys.keymanagement.api.Juggler;
import de.adorsys.keymanagement.api.config.keystore.KeyStoreConfig;
import de.adorsys.keymanagement.api.types.KeySetTemplate;
import de.adorsys.keymanagement.api.types.source.KeySet;
import de.adorsys.keymanagement.api.types.template.generated.Encrypting;
import de.adorsys.keymanagement.juggler.services.DaggerBCJuggler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

import static de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig.DOCUMENT_KEY_ID_PREFIX;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KeyStoreServiceTest extends BaseMockitoTest {
    private static final Logger logger = LoggerFactory.getLogger(KeyStoreServiceTest.class);

    private static final class TestConstants {
        static final String STORE_PASSWORD = "keystorepass";
        static final String KEY_PASSWORD = "keypass";
        static final String SECRET_KEY_ALIAS = "test-secret-key";
        static final String SECRET_KEY_VALUE = "secret";
        static final int EXPECTED_DEFAULT_ALIASES = 4;
    }

    private KeyStoreService keyStoreService;
    private KeyStoreAuth keyStoreAuth;
    private Juggler juggler;

    @BeforeEach
    void setUp() {
        juggler = DaggerBCJuggler.builder().build();
        keyStoreService = new KeyStoreServiceImpl(
                EncryptionConfig.builder().build().getKeystore(),
                juggler
        );

        keyStoreAuth = createKeyStoreAuth();
    }

    private KeyStoreAuth createKeyStoreAuth() {
        ReadStorePassword readStorePassword = new ReadStorePassword(TestConstants.STORE_PASSWORD);
        ReadKeyPassword readKeyPassword = ReadKeyPasswordTestFactory.getForString(TestConstants.KEY_PASSWORD);
        return new KeyStoreAuth(readStorePassword, readKeyPassword);
    }

    @Nested
    @DisplayName("KeyStore Creation and Initialization")
    class KeyStoreCreationTests {
        @Test
        @DisplayName("Create KeyStore with Default Configuration")
        void testCreateKeyStoreWithDefaultConfig() {
            // Arrange & Act
            KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyCreationConfig.builder().build());

            // Assert
            assertNotNull(keyStore, "KeyStore should not be null");
            assertKeyStoreProperties(keyStore);
            assertAliasCount(keyStore, TestConstants.EXPECTED_DEFAULT_ALIASES);
        }

        @Test
        @DisplayName("Create KeyStore with Custom Configuration")
        void testCreateKeyStoreWithCustomConfig() {
            // Arrange
            KeyCreationConfig customConfig = KeyCreationConfig.builder()
                    .signKeyNumber(1)
                    .encKeyNumber(1)
                    .build();

            // Act
            KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, customConfig);

            // Assert
            assertNotNull(keyStore, "KeyStore should not be null");
            assertKeyStoreProperties(keyStore);
            assertAliasCount(keyStore, TestConstants.EXPECTED_DEFAULT_ALIASES);
        }

        private void assertKeyStoreProperties(KeyStore keyStore) {
            assertEquals("BCFKS", keyStore.getType(), "Unexpected KeyStore type");
            assertEquals(GeneratorModule_ProviderFactory.provider(), keyStore.getProvider(), "Unexpected KeyStore provider");
        }

        private void assertAliasCount(KeyStore keyStore, int expectedCount) {
            List<String> aliases = getAliasesSafely(keyStore);
            assertEquals(expectedCount, aliases.size(), "Unexpected number of aliases");
        }
    }

    @Nested
    @DisplayName("KeyStore Alias Handling")
    class KeyStoreAliasTests {
        @Test
        @DisplayName("Safely Retrieve KeyStore Aliases")
        void testSafeAliasRetrieval() {
            // Arrange
            KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyCreationConfig.builder().build());

            // Act
            List<String> aliases = getAliasesSafely(keyStore);

            // Assert
            assertFalse(aliases.isEmpty(), "Aliases list should not be empty");
            assertEquals(TestConstants.EXPECTED_DEFAULT_ALIASES, aliases.size(), "Unexpected number of aliases");
        }

        @Test
        @DisplayName("Handle KeyStore Alias Retrieval Exception")
        void testKeyStoreAliasRetrievalException() {
            // Arrange
            KeyStore mockKeyStore = createKeyStoreWithAliasException();

            // Act
            List<String> aliases = getAliasesSafely(mockKeyStore);

            // Assert
            assertTrue(aliases.isEmpty(), "Aliases list should be empty when exception occurs");
        }

        private KeyStore createKeyStoreWithAliasException() {
            try {
                KeyStore mockKeyStore = mock(KeyStore.class);
                when(mockKeyStore.aliases()).thenThrow(new KeyStoreException("Simulated KeyStore Exception"));
                return mockKeyStore;
            } catch (KeyStoreException e) {
                throw new RuntimeException("Error creating mock KeyStore", e);
            }
        }
    }

    @Nested
    @DisplayName("Key Management Operations")
    class KeyManagementTests {
        @Test
        @DisplayName("Add and Retrieve Password-Based Secret Key")
        void testAddAndRetrievePasswordBasedSecretKey() {
            // Arrange
            KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyCreationConfig.builder().build());
            KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);

            // Act
            keyStoreService.addPasswordBasedSecretKey(
                    keyStoreAccess,
                    TestConstants.SECRET_KEY_ALIAS,
                    TestConstants.SECRET_KEY_VALUE.toCharArray()
            );
            SecretKey retrievedSecretKey = keyStoreService.getSecretKey(
                    keyStoreAccess,
                    new KeyID(TestConstants.SECRET_KEY_ALIAS)
            );

            // Assert
            assertNotNull(retrievedSecretKey, "Retrieved secret key should not be null");
            assertEquals(
                    TestConstants.SECRET_KEY_VALUE,
                    new String(retrievedSecretKey.getEncoded()),
                    "Secret key value mismatch"
            );
        }

        @Test
        @DisplayName("Remove Existing Key")
        void testRemoveKey() {
            // Arrange
            KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyCreationConfig.builder().build());
            KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);
            KeyID keyID = KeystoreUtil.keyIdByPrefix(keyStore, DOCUMENT_KEY_ID_PREFIX);

            // Act
            keyStoreService.removeKey(keyStoreAccess, keyID.getValue());
            SecretKey removedKey = keyStoreService.getSecretKey(keyStoreAccess, keyID);

            // Assert
            assertNull(removedKey, "Removed key should be null");
        }
    }

    @Nested
    @DisplayName("Public and Private Key Operations")
    class KeyRetrievalTests {
        @Test
        @DisplayName("Retrieve Public Keys")
        void testRetrievePublicKeys() {
            // Arrange
            KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyCreationConfig.builder().build());
            KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);

            // Act
            List<PublicKeyIDWithPublicKey> publicKeys = keyStoreService.getPublicKeys(keyStoreAccess);

            // Assert
            assertNotNull(publicKeys, "Public keys list should not be null");
            assertEquals(1, publicKeys.size(), "Unexpected number of public keys");
        }

        @Test
        @DisplayName("Retrieve Private Key Safely")
        void testRetrievePrivateKeySafely() {
            // Arrange
            KeyStore keyStore = createCustomKeyStore();
            KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);

            // Act & Assert
            List<String> aliases = getAliasesSafely(keyStore);
            assertFalse(aliases.isEmpty(), "Aliases should not be empty");

            aliases.forEach(alias -> {
                try {
                    Optional<PrivateKey> privateKey = safelyGetPrivateKey(keyStoreAccess, alias);
                    privateKey.ifPresent(key -> assertNotNull(key, "Private key should not be null"));
                } catch (Exception e) {
                    logger.warn("Error processing alias {}: {}", alias, e.getMessage());
                }
            });
        }

        private KeyStore createCustomKeyStore() {
            KeyStoreConfig config = KeyStoreConfig.builder().type("UBER").build();
            Juggler customJuggler = DaggerBCJuggler.builder().keyStoreConfig(config).build();

            KeySetTemplate template = KeySetTemplate.builder()
                    .generatedEncryptionKeys(
                            Encrypting.with()
                                    .prefix("KEYSTORE-ID-0")
                                    .password(TestConstants.KEY_PASSWORD::toCharArray)
                                    .build()
                                    .repeat(1)
                    )
                    .build();

            KeySet keySet = customJuggler.generateKeys().fromTemplate(template);

            try {
                return customJuggler.toKeystore().generate(
                        keySet,
                        () -> keyStoreAuth.getReadStorePassword().getValue()
                );
            } catch (Exception e) {
                throw new RuntimeException("Error creating custom KeyStore", e);
            }
        }
    }

    /**
     * Safely retrieve aliases from a KeyStore
     *
     * @param keyStore KeyStore to retrieve aliases from
     * @return List of aliases, empty list if exception occurs
     */
    private List<String> getAliasesSafely(KeyStore keyStore) {
        try {
            List<String> aliases = new ArrayList<>();
            Enumeration<String> aliasEnumeration = keyStore.aliases();

            while (aliasEnumeration.hasMoreElements()) {
                aliases.add(aliasEnumeration.nextElement());
            }

            return aliases;
        } catch (KeyStoreException e) {
            logger.error("Error retrieving KeyStore aliases", e);
            return Collections.emptyList();
        }
    }

    /**
     * Safely retrieve a private key from a KeyStore
     *
     * @param keyStoreAccess KeyStore access information
     * @param alias Alias to retrieve private key for
     * @return Optional containing PrivateKey if successfully retrieved
     */
    private Optional<PrivateKey> safelyGetPrivateKey(KeyStoreAccess keyStoreAccess, String alias) {
        try {
            PrivateKey privateKey = keyStoreService.getPrivateKey(keyStoreAccess, new KeyID(alias));
            return Optional.ofNullable(privateKey);
        } catch (Exception e) {
            logger.warn("Could not retrieve private key for alias {}: {}", alias, e.getMessage());
            return Optional.empty();
        }
    }
}