package de.adorsys.datasafe.business.impl.service;

import de.adorsys.datasafe.business.api.DocusafeServiceDI;
import de.adorsys.datasafe.business.api.deployment.desired.DSDocument;
import de.adorsys.datasafe.business.api.deployment.desired.DocumentDirectoryFQN;
import de.adorsys.datasafe.business.api.deployment.desired.DocumentFQN;
import de.adorsys.datasafe.business.api.types.DFSCredentials;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.dfs.connection.api.types.ListRecursiveFlag;

import java.util.List;
import java.util.stream.Collectors;

public class DocusafeServiceDIImpl implements DocusafeServiceDI {

    private DefaultDocusafeService docusafeService = DaggerDefaultDocusafeService
            .builder()
            .build();

    /**
     * Essentially means update user profile with following access details.
     */
    @Override
    public void registerDFS(DFSCredentials dfsCredentials, UserIDAuth userIDAuth) {
        throw new UnsupportedOperationException("Register DFS credentials!");
    }

    @Override
    public void storeDocument(DSDocument dsDocument, UserIDAuth userIDAuth) {
        throw new UnsupportedOperationException("Private storage access!");
    }

    @Override
    public DSDocument readDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        throw new UnsupportedOperationException("Private storage access!");
    }

    @Override
    public void deleteDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        throw new UnsupportedOperationException("Private storage access!");
    }

    @Override
    public boolean documentExists(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        throw new UnsupportedOperationException("Private storage access!");
    }

    @Override
    public void deleteFolder(DocumentDirectoryFQN documentDirectoryFQN, UserIDAuth userIDAuth) {
        throw new UnsupportedOperationException("Private storage access!");
    }

    @Override
    public List<DocumentFQN> list(DocumentDirectoryFQN documentDirectoryFQN, UserIDAuth userIDAuth, ListRecursiveFlag recursiveFlag) {
        throw new UnsupportedOperationException("Private storage access!");
    }

    @Override
    public List<DocumentFQN> listInbox(UserIDAuth userIDAuth) {
        return docusafeService.inboxService().list(userIDAuth).collect(Collectors.toList());
    }

    @Override
    public void writeDocumentToInboxOfUser(DSDocument document, UserID receiverUserID, DocumentFQN destDocumentFQN) {
        docusafeService.inboxService().write();
    }

    @Override
    public DSDocument readDocumentFromInbox(DocumentFQN source, UserIDAuth userIDAuth) {
        docusafeService.inboxService().read();
    }

    @Override
    public void deleteDocumentFromInbox(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        docusafeService.inboxService().deleteDocument();
    }

    @Override
    public void createUser(UserIDAuth userIDAuth) {
        docusafeService.userProfile().createUser();
    }

    @Override
    public void destroyUser(UserIDAuth userIDAuth) {
        docusafeService.userProfile().deregister(userIDAuth);
    }

    @Override
    public boolean userExists(UserID userID) {
        return docusafeService.userProfile().userExists(userID);
    }
}
