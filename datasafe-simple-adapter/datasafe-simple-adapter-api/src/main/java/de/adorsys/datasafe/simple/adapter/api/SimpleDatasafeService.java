package de.adorsys.datasafe.simple.adapter.api;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.simple.adapter.api.types.*;

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

    void deleteDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN);

    boolean documentExists(UserIDAuth userIDAuth, DocumentFQN documentFQN);

    void deleteFolder(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN);

    List<DocumentFQN> list(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag);
}

