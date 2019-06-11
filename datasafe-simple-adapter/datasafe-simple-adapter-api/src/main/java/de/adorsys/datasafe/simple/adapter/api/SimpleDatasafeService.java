package de.adorsys.datasafe.simple.adapter.api;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.simple.adapter.api.types.*;

import java.util.List;

public interface SimpleDatasafeService {
    /**
     * User
     */
    void createUser(UserIDAuth userIDAuth);

    void destroyUser(UserIDAuth userIDAuth);

    boolean userExists(UserID userID);

    void registerDFSCredentials (UserIDAuth userIDAuth, DFSCredentials dfsCredentials);
    /**
     * Document
     */
    void storeDocument(UserIDAuth userIDAuth, DSDocument dsDocument);

    DSDocument readDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN);

    void storeDocumentStream(UserIDAuth userIDAuth, DSDocumentStream dsDocumentStream);

    DSDocumentStream readDocumentStream(UserIDAuth userIDAuth, DocumentFQN documentFQN);

    void deleteDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN);

    boolean documentExists(UserIDAuth userIDAuth, DocumentFQN documentFQN);

    void deleteFolder(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN);

    List<DocumentFQN> list(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag);

    /**
     * InboxStuff
     */
    List<DocumentFQN> listInbox(UserIDAuth userIDAuth);

    void writeDocumentToInboxOfUser(UserID receiverUserID, DSDocument document, DocumentFQN destDocumentFQN);

    DSDocument readDocumentFromInbox(UserIDAuth userIDAuth, DocumentFQN source);

    void deleteDocumentFromInbox(UserIDAuth userIDAuth, DocumentFQN documentFQN);

    /**
     * conveniance methods
     */
    void moveDocumnetToInboxOfUser(UserIDAuth userIDAuth, UserID receiverUserID, DocumentFQN sourceDocumentFQN, DocumentFQN destDocumentFQN, MoveType moveType);

    DSDocument moveDocumentFromInbox(UserIDAuth userIDAuth, DocumentFQN source, DocumentFQN destination);
}

