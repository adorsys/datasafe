package de.adorsys.datasafe.business.api.profile.operations;

import de.adorsys.datasafe.business.api.types.UserIDAuth;

public interface ProfileRemovalService {

    void deregister(UserIDAuth userID);
}
