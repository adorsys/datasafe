package de.adorsys.datasafe.business.impl.service.desired;

import de.adorsys.datasafe.business.api.types.DFSCredentials;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.dfs.connection.api.types.ListRecursiveFlag;

import java.util.List;


// TODO: This is the target:
public interface DocusafeServiceCompat {

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
