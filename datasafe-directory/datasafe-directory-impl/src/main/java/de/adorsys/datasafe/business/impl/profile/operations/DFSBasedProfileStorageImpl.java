package de.adorsys.datasafe.business.impl.profile.operations;

import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.business.api.encryption.keystore.KeyStoreService;
import de.adorsys.datasafe.business.api.profile.operations.ProfileRegistrationService;
import de.adorsys.datasafe.business.api.profile.operations.ProfileRemovalService;
import de.adorsys.datasafe.business.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.storage.StorageListService;
import de.adorsys.datasafe.business.api.storage.StorageReadService;
import de.adorsys.datasafe.business.api.storage.StorageRemoveService;
import de.adorsys.datasafe.business.api.storage.StorageWriteService;
import de.adorsys.datasafe.business.api.version.types.*;
import de.adorsys.datasafe.business.api.version.types.action.ListRequest;
import de.adorsys.datasafe.business.api.version.types.keystore.KeyStoreAuth;
import de.adorsys.datasafe.business.api.version.types.keystore.KeyStoreCreationConfig;
import de.adorsys.datasafe.business.api.version.types.keystore.KeyStoreType;
import de.adorsys.datasafe.business.api.version.types.resource.*;
import de.adorsys.datasafe.business.impl.profile.serde.GsonSerde;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.KeyStore;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FIXME: it should be broken down.
 */
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
    private final KeyStoreService keyStoreService;
    private final DFSSystem dfsSystem;
    private final GsonSerde serde;
    private final UserProfileCache userProfileCache;

    @Inject
    public DFSBasedProfileStorageImpl(StorageReadService readService, StorageWriteService writeService,
                                      StorageRemoveService removeService, StorageListService listService,
                                      KeyStoreService keyStoreService, DFSSystem dfsSystem, GsonSerde serde,
                                      UserProfileCache userProfileCache) {
        this.readService = readService;
        this.writeService = writeService;
        this.removeService = removeService;
        this.listService = listService;
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
    }

    @Override
    @SneakyThrows
    public void registerPrivate(CreateUserPrivateProfile profile) {
        try (OutputStream os = writeService.write(locatePrivateProfile(profile.getId().getUserID()))) {
            os.write(serde.toJson(profile.removeAccess()).getBytes());
        }

        // TODO: check if we need to create it
        createKeyStore(
                profile.getId(),
                profile.getKeystore().getResource()
        );
    }

    @Override
    public void deregister(UserIDAuth userID) {
        ListRequest<UserIDAuth, AbsoluteResourceLocation<PrivateResource>> privateRequest =
                new ListRequest<>(userID, privateProfile(userID).getPrivateStorage());

        List<AbsoluteResourceLocation<PrivateResource>> privateFiles =
                listService.list(privateRequest.getLocation()).collect(Collectors.toList());

        for (AbsoluteResourceLocation<PrivateResource> file : privateFiles) {
            removeService.remove(file);
        }

        removeService.remove(privateProfile(userID).getKeystore());
        removeService.remove(privateProfile(userID).getPrivateStorage());
        removeService.remove(privateProfile(userID).getInboxWithWriteAccess());
        removeService.remove(locatePrivateProfile(userID.getUserID()));
        removeService.remove(locatePublicProfile(userID.getUserID()));
    }

    @Override
    @SneakyThrows
    public UserPublicProfile publicProfile(UserID ofUser) {
        return userProfileCache.getPublicProfile().computeIfAbsent(
                ofUser,
                id -> readProfile(locatePublicProfile(ofUser), UserPublicProfile.class)
        );
    }

    @Override
    @SneakyThrows
    public UserPrivateProfile privateProfile(UserIDAuth ofUser) {
        return userProfileCache.getPrivateProfile().computeIfAbsent(
                ofUser.getUserID(),
                id -> readProfile(locatePrivateProfile(ofUser.getUserID()), UserPrivateProfile.class)
        );
    }

    @Override
    public boolean userExists(UserID ofUser) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @SneakyThrows
    private void createKeyStore(UserIDAuth forUser, PrivateResource keystore) {
        KeyStoreAuth auth = dfsSystem.privateKeyStoreAuth(forUser);

        KeyStore store = keyStoreService.createKeyStore(
                auth,
                KeyStoreType.DEFAULT,
                new KeyStoreCreationConfig(1, 1, 1)
        );

        try (OutputStream os = writeService.write(new AbsoluteResourceLocation<>(keystore))) {
            os.write(keyStoreService.serialize(store, forUser.getUserID().getValue(), auth.getReadStorePassword()));
        }
    }

    @SneakyThrows
    private <T> T readProfile(AbsoluteResourceLocation resource, Class<T> clazz) {
        try (InputStream is = readService.read(resource)) {
            return serde.fromJson(new String(ByteStreams.toByteArray(is)), clazz);
        }
    }

    private AbsoluteResourceLocation<PrivateResource> locatePrivateProfile(UserID ofUser) {
        return new AbsoluteResourceLocation<>(
                new DefaultPrivateResource(PRIVATE.resolve(ofUser.getValue())).resolve(dfsSystem.dfsRoot())
        );
    }

    private AbsoluteResourceLocation<PublicResource> locatePublicProfile(UserID ofUser) {
        return new AbsoluteResourceLocation<>(
                new DefaultPublicResource(PUBLIC.resolve(ofUser.getValue())).resolve(dfsSystem.dfsRoot())
        );
    }
}
