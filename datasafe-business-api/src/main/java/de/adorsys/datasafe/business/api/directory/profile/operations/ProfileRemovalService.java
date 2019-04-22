package de.adorsys.datasafe.business.api.directory.profile.operations;

import de.adorsys.datasafe.business.api.types.UserIDAuth;

public interface ProfileRemovalService {

    void deregister(UserIDAuth userID);
}
