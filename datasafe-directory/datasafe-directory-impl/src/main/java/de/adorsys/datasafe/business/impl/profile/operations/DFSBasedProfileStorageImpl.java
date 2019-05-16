package de.adorsys.datasafe.business.impl.profile.operations;

import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.business.api.encryption.keystore.KeyStoreService;
import de.adorsys.datasafe.business.api.profile.operations.ProfileRegistrationService;
import de.adorsys.datasafe.business.api.profile.operations.ProfileRemovalService;
import de.adorsys.datasafe.business.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.storage.actions.*;
import de.adorsys.datasafe.business.api.types.*;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.keystore.KeyStoreAuth;
import de.adorsys.datasafe.business.api.types.keystore.KeyStoreCreationConfig;
import de.adorsys.datasafe.business.api.types.keystore.KeyStoreType;
import de.adorsys.datasafe.business.api.types.resource.*;
import de.adorsys.datasafe.business.impl.profile.exceptions.UserNotFoundException;
import de.adorsys.datasafe.business.impl.profile.serde.GsonSerde;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.KeyStore;

/**
 * FIXME: it should be broken down.
 */

@Slf4j
public class DFSBasedProfileStorageImpl implements
        ProfileRegistrationService,
        ProfileRetrievalService,
        ProfileRemovalService {

    private static final URI PRIVATE = URI.create("./profiles/private/");
    private static final URI PUBLIC = URI.create("./profiles/public/");

    private final StorageReadService readService;
    private final StorageWriteService writeService;
    private final StorageRemoveService removeService;
    private final StorageListService listService;
    private final StorageCheckService checkService;

    private final KeyStoreService keyStoreService;
    private final DFSSystem dfsSystem;
    private final GsonSerde serde;
    private final UserProfileCache userProfileCache;

    @Inject
    public DFSBasedProfileStorageImpl(StorageReadService readService, StorageWriteService writeService,
                                      StorageRemoveService removeService, StorageListService listService,
                                      StorageCheckService checkService, KeyStoreService keyStoreService,
                                      DFSSystem dfsSystem, GsonSerde serde, UserProfileCache userProfileCache) {
        this.readService = readService;
        this.writeService = writeService;
        this.removeService = removeService;
        this.listService = listService;
        this.checkService = checkService;
        this.keyStoreService = keyStoreService;
        this.dfsSystem = dfsSystem;
        this.serde = serde;
        this.userProfileCache = userProfileCache;
    }

    @Override
    @SneakyThrows
    public void registerPublic(CreateUserPublicProfile profile) {
        try (OutputStream os = writeService.write(locatePublicProfile(profile.getId()))) {
            os.write(serde.toJson(profile.removeAccess()).getBytes());
        }
        log.debug("Register public {}", profile.getId());
    }

    @Override
    @SneakyThrows
    public void registerPrivate(CreateUserPrivateProfile profile) {
        try (OutputStream os = writeService.write(locatePrivateProfile(profile.getId().getUserID()))) {
            os.write(serde.toJson(profile.removeAccess()).getBytes());
        }
        log.debug("Register private {}", profile.getId());

        // TODO: check if we need to create it
        createKeyStore(
                profile.getId(),
                profile.getKeystore().getResource()
        );
    }

    @Override
    public void deregister(UserIDAuth userID) {
        if (!userExists(userID.getUserID())) {
            log.debug("User deregistation failed. User '{}' does not exist", userID);
            throw new UserNotFoundException("User not found: " + userID);
        }

        AbsoluteLocation<PrivateResource> privateStorage = privateProfile(userID).getPrivateStorage();
        removeStorageByLocation(userID, privateStorage);

        AbsoluteLocation<PrivateResource> inbox = privateProfile(userID).getInboxWithFullAccess();
        removeStorageByLocation(userID, inbox);

        removeService.remove(privateProfile(userID).getKeystore());
        removeService.remove(privateStorage);
        removeService.remove(inbox);
        removeService.remove(locatePrivateProfile(userID.getUserID()));
        removeService.remove(locatePublicProfile(userID.getUserID()));
        log.debug("Deregistered user {}", userID);
    }

    private void removeStorageByLocation(UserIDAuth userID, AbsoluteLocation<PrivateResource> privateStorage) {
        listService.list(new ListRequest<>(userID, privateStorage).getLocation())
                .forEach(removeService::remove);
    }

    @Override
    @SneakyThrows
    public UserPublicProfile publicProfile(UserID ofUser) {
        UserPublicProfile userPublicProfile = userProfileCache.getPublicProfile().computeIfAbsent(
                ofUser,
                id -> readProfile(locatePublicProfile(ofUser), UserPublicProfile.class)
        );
        log.debug("get public profile {} for user {}", userPublicProfile, ofUser);
        return userPublicProfile;
    }

    @Override
    @SneakyThrows
    public UserPrivateProfile privateProfile(UserIDAuth ofUser) {
        UserPrivateProfile userPrivateProfile = userProfileCache.getPrivateProfile().computeIfAbsent(
                ofUser.getUserID(),
                id -> readProfile(locatePrivateProfile(ofUser.getUserID()), UserPrivateProfile.class)
        );
        log.debug("get private profile {} for user {}", userPrivateProfile, ofUser);
        return userPrivateProfile;
    }

    @Override
    public boolean userExists(UserID ofUser) {
        return checkService.objectExists(locatePrivateProfile(ofUser)) &&
                checkService.objectExists(locatePublicProfile(ofUser));
    }

    @SneakyThrows
    private void createKeyStore(UserIDAuth forUser, PrivateResource keystore) {
        KeyStoreAuth auth = dfsSystem.privateKeyStoreAuth(forUser);

        KeyStore store = keyStoreService.createKeyStore(
                auth,
                KeyStoreType.DEFAULT,
                new KeyStoreCreationConfig(1, 1, 1)
        );

        try (OutputStream os = writeService.write(new AbsoluteLocation<>(keystore))) {
            os.write(keyStoreService.serialize(store, forUser.getUserID().getValue(), auth.getReadStorePassword()));
        }
        log.debug("Keystore created for user {} in path {}", forUser, keystore);
    }

    @SneakyThrows
    private <T> T readProfile(AbsoluteLocation resource, Class<T> clazz) {
        try (InputStream is = readService.read(resource)) {
            log.debug("read profile {}", resource.location().getPath());
            return serde.fromJson(new String(ByteStreams.toByteArray(is)), clazz);
        }
    }

    private AbsoluteLocation<PrivateResource> locatePrivateProfile(UserID ofUser) {
        return new AbsoluteLocation<>(
                new BasePrivateResource(PRIVATE.resolve(ofUser.getValue())).resolve(dfsSystem.dfsRoot())
        );
    }

    private AbsoluteLocation<PublicResource> locatePublicProfile(UserID ofUser) {
        return new AbsoluteLocation<>(
                new BasePublicResource(PUBLIC.resolve(ofUser.getValue())).resolve(dfsSystem.dfsRoot())
        );
    }
}
