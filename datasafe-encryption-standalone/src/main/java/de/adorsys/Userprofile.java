package de.adorsys;

import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.directory.api.types.CreateUserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.impl.profile.operations.actions.ProfileStoreService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;

import java.util.Vector;

public class Userprofile {
    private final DFSConfig config;
    private final ProfileStoreService storeProfile;
    private final ProfileRetrievalService retrieveProfile;

    public Userprofile(DFSConfig config, ProfileStoreService storeProfile, ProfileRetrievalService retrieveProfile) {
        this.config = config;
        this.storeProfile = storeProfile;
        this.retrieveProfile = retrieveProfile;
    }

    public void createPrivProfile(UserIDAuth user) {
        if (!userExists(user)) {
            CreateUserPrivateProfile templatePrivProfile = config.defaultPrivateTemplate(user);
            UserPrivateProfile privateProfile = templatePrivProfile.buildPrivateProfile();
            storeProfile.registerPrivate(templatePrivProfile.getId().getUserID(), privateProfile);
        }
    }

    public UserPrivateProfile getUserPrivProfile(UserIDAuth user) {
        return retrieveProfile.privateProfile(user);
    }
    private boolean userExists(UserIDAuth user) {
        return retrieveProfile.userExists(user.getUserID());
    }
}
