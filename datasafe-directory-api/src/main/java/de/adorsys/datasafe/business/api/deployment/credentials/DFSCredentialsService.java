package de.adorsys.datasafe.business.api.deployment.credentials;

import de.adorsys.datasafe.business.api.deployment.credentials.dto.DFSCredentials;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;

import java.net.URI;

public interface DFSCredentialsService {

    DFSCredentials privateUserCredentials(UserIDAuth forUser, URI forBucket);
    DFSCredentials publicUserCredentials(UserID forUser, URI forBucket);
}
