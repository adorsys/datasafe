package de.adorsys.datasafe.business.impl.profile;

import de.adorsys.datasafe.business.api.profile.UserCreationService;
import de.adorsys.datasafe.business.api.profile.UserProfileService;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.profile.UserPrivateProfile;
import de.adorsys.datasafe.business.api.types.profile.UserPublicProfile;
import de.adorsys.dfs.connection.api.complextypes.BucketPath;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// DEPLOYMENT
@Singleton
public class HashMapProfileStorageImpl implements UserCreationService, UserProfileService {

    private final Map<UserID, UserPublicProfile> publicProfiles = new ConcurrentHashMap<>();
    private final Map<UserID, UserPrivateProfile> privateProfiles = new ConcurrentHashMap<>();

    @Inject
    public HashMapProfileStorageImpl() {
    }

    @Override
    public void registerPublic(UserID ofUser, UserPublicProfile publicProfile) {
        publicProfiles.put(ofUser, publicProfile);
    }

    @Override
    public void registerPrivate(UserIDAuth ofUser, UserPrivateProfile privateProfile) {
        privateProfiles.put(ofUser.getUserID(), privateProfile);
    }

    @Override
    public UserPublicProfile publicProfile(UserID ofUser) {
        return publicProfiles.get(ofUser);
    }

    @Override
    public UserPrivateProfile privateProfile(UserIDAuth ofUser) {
        return privateProfiles.get(ofUser.getUserID());
    }

    public void register(UserIDAuth user) {
        registerPublic(user.getUserID(), new UserPublicProfile(
                of(user, "public_keys"),
                of(user, "inbox")
        ));

        registerPrivate(user, new UserPrivateProfile(
                of(user, "keystore"),
                of(user, "private")
        ));
    }

    private BucketPath of(UserIDAuth user, String subDir) {
        BucketPath base = new BucketPath("file://");
        return base.append(user.getUserID().getValue()).append(subDir);
    }
}
