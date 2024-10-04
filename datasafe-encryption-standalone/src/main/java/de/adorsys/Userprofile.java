package de.adorsys;

import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.api.types.CreateUserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.impl.profile.operations.actions.ProfileStoreService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;

import java.util.Vector;

public class Userprofile {
    private DFSConfig config;
    private ProfileStoreService storeProfile;
    private UserPrivateProfile privateProfile;
    private Vector<CreateUserPrivateProfile> userPrivateProfiles = new Vector<CreateUserPrivateProfile>();
    public Userprofile (DFSConfig config, ProfileStoreService storeProfile) {
        this.config = config;
        this.storeProfile = storeProfile;
    }

    public void createProfile(UserIDAuth user) {
        CreateUserPrivateProfile templatePrivProfile = config.defaultPrivateTemplate(user);
        userPrivateProfiles.add(templatePrivProfile);
        privateProfile = templatePrivProfile.buildPrivateProfile();
        storeProfile.registerPrivate(templatePrivProfile.getId().getUserID(), privateProfile);
    }

    public UserPrivateProfile getUserProfile(UserIDAuth user) {
        return findUserProfile(user);
    }
    private UserPrivateProfile findUserProfile(UserIDAuth user) {
        for (CreateUserPrivateProfile profile : userPrivateProfiles) {
            if (profile.getId().getUserID().equals(user.getUserID())) {
                return profile.buildPrivateProfile();
            }
        }
        return null;
    }
}
