package de.adorsys.datasafe.directory.impl.profile.operations.actions;

import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRegistrationService;
import de.adorsys.datasafe.directory.api.types.CreateUserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.CreateUserPublicProfile;
import de.adorsys.datasafe.directory.impl.profile.serde.GsonSerde;
import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.keystore.*;
import de.adorsys.datasafe.storage.api.actions.StorageCheckService;
import de.adorsys.datasafe.storage.api.actions.StorageWriteService;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.ResourceLocation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.OutputStream;
import java.security.KeyStore;
import java.util.List;

@Slf4j
public class ProfileRegistrationServiceImpl implements ProfileRegistrationService {

    private final KeyStoreService keyStoreService;
    private final StorageCheckService checkService;
    private final StorageWriteService writeService;
    private final GsonSerde serde;
    private final DFSConfig dfsConfig;

    @Inject
    public ProfileRegistrationServiceImpl(KeyStoreService keyStoreService, StorageCheckService checkService,
                                          StorageWriteService writeService, GsonSerde serde, DFSConfig dfsConfig) {
        this.keyStoreService = keyStoreService;
        this.checkService = checkService;
        this.writeService = writeService;
        this.serde = serde;
        this.dfsConfig = dfsConfig;
    }

    /**
     * Register users' public profile at the location specified by {@link DFSConfig}, overwrites it if exists.
     */
    @Override
    @SneakyThrows
    public void registerPublic(CreateUserPublicProfile profile) {
        try (OutputStream os = writeService.write(dfsConfig.publicProfile(profile.getId()))) {
            os.write(serde.toJson(profile.removeAccess()).getBytes());
        }
        log.debug("Register public {}", profile.getId());
    }

    /**
     * Register users' private profile at the location specified by {@link DFSConfig}, creates keystore and publishes
     * public keys, but only if keystore doesn't exist.
     */
    @Override
    @SneakyThrows
    public void registerPrivate(CreateUserPrivateProfile profile) {
        try (OutputStream os = writeService.write(dfsConfig.privateProfile(profile.getId().getUserID()))) {
            os.write(serde.toJson(profile.removeAccess()).getBytes());
        }
        log.debug("Register private {}", profile.getId());

        if (checkService.objectExists(profile.getKeystore())) {
            return;
        }

        List<PublicKeyIDWithPublicKey> publicKeys = createKeyStore(
                profile.getId().getUserID(),
                dfsConfig.privateKeyStoreAuth(profile.getId()),
                profile.getKeystore()
        );

        publishPublicKeysIfNeeded(profile.getPublishPubKeysTo(), publicKeys);
    }

    @SneakyThrows
    private <T extends ResourceLocation<T>> List<PublicKeyIDWithPublicKey> createKeyStore(
            UserID forUser, KeyStoreAuth auth, AbsoluteLocation<T> keystore) {
        KeyStore keystoreBlob = keyStoreService.createKeyStore(
                auth,
                KeyStoreType.DEFAULT,
                new KeyStoreCreationConfig(1, 1)
        );

        try (OutputStream os = writeService.write(keystore)) {
            os.write(keyStoreService.serialize(keystoreBlob, forUser.getValue(), auth.getReadStorePassword()));
        }
        log.debug("Keystore created for user {} in path {}", forUser, keystore);

        return keyStoreService.getPublicKeys(new KeyStoreAccess(keystoreBlob, auth));
    }

    @SneakyThrows
    private void publishPublicKeysIfNeeded(AbsoluteLocation publishTo,
                                           List<PublicKeyIDWithPublicKey> publicKeys) {

        if (null != publishTo && !checkService.objectExists(publishTo)) {
            try (OutputStream os = writeService.write(publishTo)) {
                os.write(serde.toJson(publicKeys).getBytes());
            }
        }
    }
}
