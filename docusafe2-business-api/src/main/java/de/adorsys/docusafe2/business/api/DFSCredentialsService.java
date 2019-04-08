package de.adorsys.docusafe2.business.api;

import de.adorsys.docusafe2.business.api.types.UserIDAuth;

public interface DFSCredentialsService {
    public DFSCredentials getDFSCredentials (UserIDAuth userIDAuth);
    public void registerDFS(DFSCredentials dfsCredentials, UserIDAuth userIDAuth);
}
