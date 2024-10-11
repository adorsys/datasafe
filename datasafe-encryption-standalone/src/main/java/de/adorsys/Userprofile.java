package de.adorsys;

import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.directory.api.types.CreateUserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.impl.profile.operations.actions.ProfileStoreService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;

import java.util.Vector;

public class Userprofile {
    private DFSConfig config;
    private ProfileStoreService storeProfile;
    private ProfileRetrievalService retrieveProfile;
    private UserPrivateProfile privateProfile;

    public Userprofile(DFSConfig config, ProfileStoreService storeProfile, ProfileRetrievalService retrieveProfile) {
        this.config = config;
        this.storeProfile = storeProfile;
        this.retrieveProfile = retrieveProfile;
    }

    public void createProfile(UserIDAuth user) {
        if (!retrieveProfile.userExists(user.getUserID())) {
            CreateUserPrivateProfile templatePrivProfile = config.defaultPrivateTemplate(user);
            privateProfile = templatePrivProfile.buildPrivateProfile();
            storeProfile.registerPrivate(templatePrivProfile.getId().getUserID(), privateProfile);
        }
    }

    public UserPrivateProfile getUserProfile(UserIDAuth user) {
        return retrieveProfile.privateProfile(user);
    }

}
