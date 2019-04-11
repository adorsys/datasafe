package de.adorsys.datasafe.business.api.dfscredentialservice;

import de.adorsys.datasafe.business.api.types.DFSCredentials;
import de.adorsys.datasafe.business.api.types.UserIDAuth;

public interface DFSCredentialService {
    DFSCredentials getDFSCredentials(UserIDAuth userIDAuth);

    void registerDFS(DFSCredentials dfsCredentials, UserIDAuth userIDAuth);
}
