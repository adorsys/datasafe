package de.adorsys.datasafe.business.api;

import de.adorsys.datasafe.business.api.deployment.desired.DSDocument;
import de.adorsys.datasafe.business.api.deployment.desired.DocumentDirectoryFQN;
import de.adorsys.datasafe.business.api.deployment.desired.DocumentFQN;
import de.adorsys.datasafe.business.api.types.DFSCredentials;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.dfs.connection.api.types.ListRecursiveFlag;

import java.util.List;

/**
 * This is same as {@link DocusafeServiceOrig} but implemented with dagger and modules
 */
public interface DocusafeServiceDI {

    void registerDFS(DFSCredentials dfsCredentials, UserIDAuth userIDAuth);

    void storeDocument(DSDocument dsDocument, UserIDAuth userIDAuth);

    DSDocument readDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN);

    void deleteDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN);

    boolean documentExists(UserIDAuth userIDAuth, DocumentFQN documentFQN);

    void deleteFolder(DocumentDirectoryFQN documentDirectoryFQN, UserIDAuth userIDAuth);

    List<DocumentFQN> list(DocumentDirectoryFQN documentDirectoryFQN, UserIDAuth userIDAuth, ListRecursiveFlag recursiveFlag);

    List<DocumentFQN> listInbox (UserIDAuth userIDAuth);

    void writeDocumentToInboxOfUser (DSDocument document, UserID receiverUserID, DocumentFQN destDocumentFQN);

    DSDocument readDocumentFromInbox (DocumentFQN source, UserIDAuth userIDAuth);

    void deleteDocumentFromInbox (UserIDAuth userIDAuth, DocumentFQN documentFQN);

    void createUser(UserIDAuth userIDAuth);

    void destroyUser(UserIDAuth userIDAuth);

    boolean userExists(UserID userID);
}
