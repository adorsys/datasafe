package de.adorsys.datasafe.business.impl.profile;

import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.business.api.directory.profile.operations.ProfileRegistrationService;
import de.adorsys.datasafe.business.api.directory.profile.operations.ProfileRemovalService;
import de.adorsys.datasafe.business.api.directory.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.encryption.keystore.KeyStoreService;
import de.adorsys.datasafe.business.api.storage.StorageReadService;
import de.adorsys.datasafe.business.api.storage.StorageWriteService;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.keystore.KeyStoreAuth;
import de.adorsys.datasafe.business.api.types.keystore.KeyStoreCreationConfig;
import de.adorsys.datasafe.business.api.types.keystore.KeyStoreType;
import de.adorsys.datasafe.business.api.types.profile.CreateUserPrivateProfile;
import de.adorsys.datasafe.business.api.types.profile.CreateUserPublicProfile;
import de.adorsys.datasafe.business.api.types.profile.UserPrivateProfile;
import de.adorsys.datasafe.business.api.types.profile.UserPublicProfile;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.impl.keystore.generator.KeyStoreServiceImplBaseFunctions;
import de.adorsys.datasafe.business.impl.keystore.generator.PasswordCallbackHandler;
import de.adorsys.datasafe.business.impl.serde.GsonSerde;
import de.adorsys.datasafe.business.impl.types.DefaultPrivateResource;
import de.adorsys.datasafe.business.impl.types.DefaultPublicResource;
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

        byte[] serialized = KeyStoreServiceImplBaseFunctions.toByteArray(
            store,
            forUser.getUserID().getValue(),
            new PasswordCallbackHandler(auth.getReadStorePassword().getValue().toCharArray())
        );

        try(OutputStream os = writeService.write(keystore)) {
            os.write(serialized);
        }
    }

    private static URI locatePrivateProfile(UserID ofUser) {
        return PRIVATE.resolve(ofUser.getValue());
    }

    private static URI locatePublicProfile(UserID ofUser) {
        return PUBLIC.resolve(ofUser.getValue());
    }
}
