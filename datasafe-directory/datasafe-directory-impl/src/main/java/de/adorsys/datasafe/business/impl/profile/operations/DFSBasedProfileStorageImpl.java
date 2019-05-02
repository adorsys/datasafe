package de.adorsys.datasafe.business.impl.profile.operations;

import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.business.api.encryption.keystore.KeyStoreService;
import de.adorsys.datasafe.business.api.profile.operations.ProfileRegistrationService;
import de.adorsys.datasafe.business.api.profile.operations.ProfileRemovalService;
import de.adorsys.datasafe.business.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.storage.StorageReadService;
import de.adorsys.datasafe.business.api.storage.StorageWriteService;
import de.adorsys.datasafe.business.api.types.*;
import de.adorsys.datasafe.business.api.types.keystore.KeyStoreAuth;
import de.adorsys.datasafe.business.api.types.keystore.KeyStoreCreationConfig;
import de.adorsys.datasafe.business.api.types.keystore.KeyStoreType;
import de.adorsys.datasafe.business.api.types.resource.DefaultPrivateResource;
import de.adorsys.datasafe.business.api.types.resource.DefaultPublicResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.impl.profile.serde.GsonSerde;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.KeyStore;

/**
 * This is approximation of real implementation - should be broken down.
 */
public class DFSBasedProfileStorageImpl implements
        ProfileRegistrationService,
        ProfileRetrievalService,
        ProfileRemovalService {

    private static final URI PRIVATE = URI.create("./profiles/private/");
    private static final URI PUBLIC = URI.create("./profiles/public/");

    private final StorageReadService readService;
    private final StorageWriteService writeService;
    private final KeyStoreService keyStoreService;
    private final DFSSystem dfsSystem;
    private final GsonSerde serde;

    @Inject
    public DFSBasedProfileStorageImpl(StorageReadService readService, StorageWriteService writeService,
                                      KeyStoreService keyStoreService, DFSSystem dfsSystem, GsonSerde serde) {
        this.readService = readService;
        this.writeService = writeService;
        this.keyStoreService = keyStoreService;
        this.dfsSystem = dfsSystem;
        this.serde = serde;
    }

    @Override
    @SneakyThrows
    public void registerPublic(CreateUserPublicProfile profile) {

        try (OutputStream os = writeService.write(
                new DefaultPrivateResource(locatePublicProfile(profile.getId())))
        ) {
            os.write(serde.toJson(profile.removeAccess()).getBytes());
        }
    }

    @Override
    @SneakyThrows
    public void registerPrivate(CreateUserPrivateProfile profile) {
        try (OutputStream os = writeService.write(
                new DefaultPrivateResource(locatePrivateProfile(profile.getId().getUserID())))
        ) {
            os.write(serde.toJson(profile.removeAccess()).getBytes());
        }

        // TODO: check if we need to create it
        createKeyStore(
            profile.getId(),
            profile.getKeystore()
        );
    }

    @Override
    public void deregister(UserIDAuth userID) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    @SneakyThrows
    public UserPublicProfile publicProfile(UserID ofUser) {
        try (InputStream is =  readService.read(
                new DefaultPublicResource(locatePublicProfile(ofUser)))
        ) {
            return serde.fromJson(new String(ByteStreams.toByteArray(is)), UserPublicProfile.class);
        }
    }

    @Override
    @SneakyThrows
    public UserPrivateProfile privateProfile(UserIDAuth ofUser) {
        try (InputStream is =  readService.read(
                new DefaultPrivateResource(locatePrivateProfile(ofUser.getUserID())))
        ) {
            return serde.fromJson(new String(ByteStreams.toByteArray(is)), UserPrivateProfile.class);
        }
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

        try (OutputStream os = writeService.write(keystore)) {
            os.write(keyStoreService.serialize(store, forUser.getUserID().getValue(), auth.getReadStorePassword()));
        }
    }

    private static URI locatePrivateProfile(UserID ofUser) {
        return PRIVATE.resolve(ofUser.getValue());
    }

    private static URI locatePublicProfile(UserID ofUser) {
        return PUBLIC.resolve(ofUser.getValue());
    }
}
