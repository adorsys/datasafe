package de.adorsys.datasafe.business.impl.profile;

import de.adorsys.datasafe.business.api.deployment.profile.ProfileRegistrationService;
import de.adorsys.datasafe.business.api.deployment.profile.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.deployment.profile.ProfileRemovalService;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.profile.UserPrivateProfile;
import de.adorsys.datasafe.business.api.types.profile.UserPublicProfile;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// DEPLOYMENT
@Singleton
public class HashMapProfileStorageImpl implements ProfileRegistrationService, ProfileRetrievalService, ProfileRemovalService {

    private final Map<UserID, UserPublicProfile> publicProfiles = new ConcurrentHashMap<>();
    private final Map<UserID, UserPrivateProfile> privateProfiles = new ConcurrentHashMap<>();

    @Inject
    public HashMapProfileStorageImpl() {
    }

    @Override
    public void registerPublic(UserID ofUser, UserPublicProfile publicProfile) {

    }

    @Override
    public void registerPrivate(UserIDAuth ofUser, UserPrivateProfile privateProfile) {

    }

    @Override
    public UserPublicProfile publicProfile(UserID ofUser) {
        return null;
    }

    @Override
    public UserPrivateProfile privateProfile(UserIDAuth ofUser) {
        return null;
    }

    @Override
    public boolean userExists(UserID ofUser) {
        return false;
    }

    @Override
    public void deregisterPublic(UserID userID) {

    }

    @Override
    public void deregisterPrivate(UserID userID) {

    }
}
