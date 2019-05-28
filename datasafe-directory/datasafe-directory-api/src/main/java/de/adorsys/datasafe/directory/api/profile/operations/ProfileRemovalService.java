package de.adorsys.datasafe.directory.api.profile.operations;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;

public interface ProfileRemovalService {

    void deregister(UserIDAuth userID);
}
