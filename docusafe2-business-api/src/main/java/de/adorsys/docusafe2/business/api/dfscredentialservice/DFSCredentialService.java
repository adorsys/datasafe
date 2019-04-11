package de.adorsys.docusafe2.business.api.dfscredentialservice;

import de.adorsys.docusafe2.business.api.types.DFSCredentials;
import de.adorsys.docusafe2.business.api.types.UserIDAuth;

public interface DFSCredentialService {
    DFSCredentials getDFSCredentials(UserIDAuth userIDAuth);

    void registerDFS(DFSCredentials dfsCredentials, UserIDAuth userIDAuth);
}
