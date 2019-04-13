package de.adorsys.datasafe.business.impl.profile;

import de.adorsys.datasafe.business.api.deployment.dfs.DFSConnectionService;
import de.adorsys.datasafe.business.api.deployment.profile.ProfileRegistrationService;
import de.adorsys.datasafe.business.api.deployment.profile.ProfileRemovalService;
import de.adorsys.datasafe.business.api.deployment.profile.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.serde.GsonSerde;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.profile.CreateUserPrivateProfile;
import de.adorsys.datasafe.business.api.types.profile.CreateUserPublicProfile;
import de.adorsys.datasafe.business.api.types.profile.UserPrivateProfile;
import de.adorsys.datasafe.business.api.types.profile.UserPublicProfile;
import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.dfs.connection.api.domain.Payload;
import de.adorsys.dfs.connection.api.service.api.DFSConnection;
import de.adorsys.dfs.connection.api.service.impl.SimplePayloadImpl;

import javax.inject.Inject;

public class DFSBasedProfileStorageImpl implements
    ProfileRegistrationService,
    ProfileRetrievalService,
    ProfileRemovalService {

    private static final BucketPath PRIVATE = new BucketPath("private");
    private static final BucketPath PUBLIC = new BucketPath("public");

    private final DFSConnectionService dfsConnectionService;
    private final DFSSystem dfsSystem;
    private final GsonSerde serde;

    @Inject
    public DFSBasedProfileStorageImpl(DFSConnectionService dfsConnectionService, DFSSystem dfsSystem, GsonSerde serde) {
        this.dfsConnectionService = dfsConnectionService;
        this.dfsSystem = dfsSystem;
        this.serde = serde;
    }


    @Override
    public void registerPublic(CreateUserPublicProfile profile) {
        DFSConnection connection = dfsConnectionService.obtain(dfsSystem.systemDfs());

        BucketPath profilePath = locatePublicProfile(profile.getId());
        connection.createContainer(profilePath.getBucketDirectory());
        connection.putBlob(
            profilePath,
            new SimplePayloadImpl(serde.toJson(profile).getBytes())
        );
    }

    @Override
    public void registerPrivate(CreateUserPrivateProfile profile) {
        DFSConnection connection = dfsConnectionService.obtain(dfsSystem.systemDfs());

        BucketPath profilePath = locatePrivateProfile(profile.getId().getUserID());
        connection.createContainer(profilePath.getBucketDirectory());
        connection.putBlob(
            profilePath,
            new SimplePayloadImpl(serde.toJson(profile).getBytes())
        );
    }

    @Override
    public void deregister(UserIDAuth userID) {
        DFSConnection connection = dfsConnectionService.obtain(dfsSystem.systemDfs());

        connection.removeBlob(locatePublicProfile(userID.getUserID()));
        connection.removeBlob(locatePrivateProfile(userID.getUserID()));
    }

    @Override
    public UserPublicProfile publicProfile(UserID ofUser) {
        DFSConnection connection = dfsConnectionService.obtain(dfsSystem.systemDfs());

        Payload publicSerialized = connection.getBlob(locatePublicProfile(ofUser));

        return serde.fromJson(new String(publicSerialized.getData()), UserPublicProfile.class);
    }

    @Override
    public UserPrivateProfile privateProfile(UserIDAuth ofUser) {
        DFSConnection connection = dfsConnectionService.obtain(dfsSystem.systemDfs());

        Payload privateSerialized = connection.getBlob(locatePrivateProfile(ofUser.getUserID()));

        return serde.fromJson(new String(privateSerialized.getData()), UserPrivateProfile.class);
    }

    @Override
    public boolean userExists(UserID ofUser) {
        DFSConnection connection = dfsConnectionService.obtain(dfsSystem.systemDfs());

        return connection.blobExists(locatePublicProfile(ofUser))
            && connection.blobExists(locatePrivateProfile(ofUser));
    }

    private static BucketPath locatePrivateProfile(UserID ofUser) {
        return PRIVATE.append(ofUser.getValue());
    }

    private static BucketPath locatePublicProfile(UserID ofUser) {
        return PUBLIC.append(ofUser.getValue());
    }
}
