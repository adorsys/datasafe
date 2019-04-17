package de.adorsys.datasafe.business.api.deployment.profile;

import de.adorsys.datasafe.business.api.types.UserIDAuth;

public interface ProfileRemovalService {

    void deregister(UserIDAuth userID);
}
