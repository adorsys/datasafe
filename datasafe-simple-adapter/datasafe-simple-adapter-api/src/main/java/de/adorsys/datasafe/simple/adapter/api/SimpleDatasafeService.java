package de.adorsys.datasafe.simple.adapter.api;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.inbox.api.InboxService;
import de.adorsys.datasafe.simple.adapter.api.types.DSDocument;
import de.adorsys.datasafe.simple.adapter.api.types.DSDocumentStream;
import de.adorsys.datasafe.simple.adapter.api.types.DocumentDirectoryFQN;
import de.adorsys.datasafe.simple.adapter.api.types.DocumentFQN;
import de.adorsys.datasafe.simple.adapter.api.types.ListRecursiveFlag;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;

import java.io.OutputStream;
import java.util.List;

public interface SimpleDatasafeService {
    /**
     * User
     */
    void createUser(UserIDAuth userIDAuth);

    void changeKeystorePassword(UserIDAuth userIDAuth, ReadKeyPassword newPassword);

    void destroyUser(UserIDAuth userIDAuth);

    boolean userExists(UserID userID);
    /**
     * Document
     */
    void storeDocument(UserIDAuth userIDAuth, DSDocument dsDocument);

    DSDocument readDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN);

    // Preferred version of store document
    OutputStream storeDocumentStream(UserIDAuth userIDAuth, DocumentFQN documentFQN);

    DSDocumentStream readDocumentStream(UserIDAuth userIDAuth, DocumentFQN documentFQN);

    // Old interface compatible store document
    void storeDocumentStream(UserIDAuth userIDAuth, DSDocumentStream dsDocumentStream);

    void deleteDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN);

    boolean documentExists(UserIDAuth userIDAuth, DocumentFQN documentFQN);

    void deleteFolder(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN);

    List<DocumentFQN> list(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag);

    InboxService getInboxService();

    // deletes all users and their files from storage
    void cleanupDb();
}

