package de.adorsys.datasafe.directory.impl.profile.operations.actions;

import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.profile.keys.KeyStoreOperations;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRegistrationService;
import de.adorsys.datasafe.directory.api.types.CreateUserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.CreateUserPublicProfile;
import de.adorsys.datasafe.directory.impl.profile.serde.GsonSerde;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.*;
import de.adorsys.datasafe.storage.api.actions.StorageCheckService;
import de.adorsys.datasafe.storage.api.actions.StorageWriteService;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.WithCallback;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.OutputStream;
import java.util.List;

@Slf4j
@RuntimeDelegate
public class ProfileRegistrationServiceImpl implements ProfileRegistrationService {

    private final KeyStoreOperations keyStoreOper;
    private final BucketAccessService access;
    private final StorageCheckService checkService;
    private final StorageWriteService writeService;
    private final GsonSerde serde;
    private final DFSConfig dfsConfig;

    @Inject
    public ProfileRegistrationServiceImpl(KeyStoreOperations keyStoreOper, BucketAccessService access,
                                          StorageCheckService checkService, StorageWriteService writeService,
                                          GsonSerde serde, DFSConfig dfsConfig) {
        this.keyStoreOper = keyStoreOper;
        this.access = access;
        this.checkService = checkService;
        this.writeService = writeService;
        this.serde = serde;
        this.dfsConfig = dfsConfig;
    }

    /**
     * Register users' public profile at the location specified by {@link DFSConfig}, overwrites it if exists.
     * IMPORTANT: For cases when user profile is stored on S3 without object locks, this requires some global
     * synchronization due to eventual consistency or you need to supply globally unique username on registration.
     */
    @Override
    @SneakyThrows
    public void registerPublic(CreateUserPublicProfile profile) {
        log.debug("Register public {}", profile);
        try (OutputStream os = writeService.write(
                WithCallback.noCallback(access.withSystemAccess(dfsConfig.publicProfile(profile.getId()))))
        ) {
            os.write(serde.toJson(profile.removeAccess()).getBytes());
        }
    }

    /**
     * Register users' private profile at the location specified by {@link DFSConfig}, creates keystore and publishes
     * public keys, but only if keystore doesn't exist.
     * IMPORTANT: For cases when user profile is stored on S3 without object locks, this requires some global
     * synchronization due to eventual consistency or you need to supply globally unique username on registration.
     */
    @Override
    @SneakyThrows
    public void registerPrivate(CreateUserPrivateProfile profile) {
        log.debug("Register private {}", profile);
        try (OutputStream os = writeService.write(
                WithCallback.noCallback(access.withSystemAccess(dfsConfig.privateProfile(profile.getId().getUserID()))))
        ) {
            os.write(serde.toJson(profile.removeAccess()).getBytes());
        }

        if (checkService.objectExists(access.withSystemAccess(profile.getKeystore()))) {
            log.warn("Keystore already exists for {} at {}, will not create new",
                    profile.getId().getUserID(), profile.getKeystore().location());
            return;
        }

        publishPublicKeysIfNeeded(
                profile.getPublishPubKeysTo(),
                keyStoreOper.createAndWriteKeyStore(profile.getId())
        );
    }

    @Override
    public void updateReadKeyPassword(UserIDAuth forUser, ReadKeyPassword newPassword) {
        keyStoreOper.updateReadKeyPassword(forUser, newPassword);
    }

    /**
     * IMPORTANT: For cases when user profile is stored on S3 without object locks, this requires some global
     * synchronization due to eventual consistency or you need to supply globally unique username on registration.
     * @param user User authorization to register
     */
    @Override
    public void registerUsingDefaults(UserIDAuth user) {
        registerPublic(dfsConfig.defaultPublicTemplate(user));
        registerPrivate(dfsConfig.defaultPrivateTemplate(user));
    }



    @SneakyThrows
    private void publishPublicKeysIfNeeded(AbsoluteLocation publishTo,
                                           List<PublicKeyIDWithPublicKey> publicKeys) {

        if (null != publishTo && !checkService.objectExists(access.withSystemAccess(publishTo))) {
            try (OutputStream os = writeService.write(WithCallback.noCallback(access.withSystemAccess(publishTo)))) {
                os.write(serde.toJson(publicKeys).getBytes());
            }
            log.debug("Public keys for published {}", publishTo);
        } else {
            log.warn("Public keys already exist, won't publish {}", publicKeys);
        }
    }
}
