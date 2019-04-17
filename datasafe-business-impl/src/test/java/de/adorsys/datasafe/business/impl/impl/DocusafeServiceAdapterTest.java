package de.adorsys.datasafe.business.impl.impl;

import de.adorsys.datasafe.business.api.deployment.desired.DSDocument;
import de.adorsys.datasafe.business.api.deployment.desired.DocumentDirectoryFQN;
import de.adorsys.datasafe.business.api.deployment.desired.DocumentFQN;
import de.adorsys.datasafe.business.api.deployment.keystore.types.ReadKeyPassword;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.impl.BaseMockitoTest;
import de.adorsys.datasafe.business.impl.service.DocusafeServiceAdapterImpl;
import de.adorsys.dfs.connection.api.types.ListRecursiveFlag;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class DocusafeServiceAdapterTest extends BaseMockitoTest {

    private static final String MESSAGE_ONE = "Hello here";

    private DocusafeServiceAdapterImpl adapter = new DocusafeServiceAdapterImpl();

    private UserIDAuth john;
    private UserIDAuth jane;

    @Test
    void testWriteToPrivateListPrivateReadPrivateAndSendToAndReadFromInbox(@TempDir Path dfsLocation) {
        System.setProperty("SC-FILESYSTEM", dfsLocation.toFile().getAbsolutePath());

        registerJohnAndJane();

        writeDataToPrivate(jane, new DSDocument(new DocumentFQN("secret.txt"), MESSAGE_ONE.getBytes()));

        DocumentFQN privateJane = getFirstFileInPrivate(jane);

        DSDocument privateContentJane = readPrivateUsingPrivateKey(jane, privateJane);

        sendToInbox(jane.getUserID(), john.getUserID(), privateContentJane);

        DocumentFQN inboxJohn = getFirstFileInInbox(john);

        DSDocument result = readInboxUsingPrivateKey(john, inboxJohn);
        assertThat(new String(result.getContent())).isEqualTo(MESSAGE_ONE);
    }

    private void writeDataToPrivate(UserIDAuth auth, DSDocument doc) {
        adapter.storeDocument(doc, auth);
    }

    private DocumentFQN getFirstFileInPrivate(UserIDAuth inboxOwner) {
        return adapter.list(new DocumentDirectoryFQN(), inboxOwner, ListRecursiveFlag.FALSE).get(0);
    }

    private DSDocument readPrivateUsingPrivateKey(UserIDAuth user, DocumentFQN location) {
        return adapter.readDocument(user, location);
    }

    private DSDocument readInboxUsingPrivateKey(UserIDAuth user, DocumentFQN location) {
        return adapter.readDocumentFromInbox(location, user);
    }

    private DocumentFQN getFirstFileInInbox(UserIDAuth inboxOwner) {
        return adapter.listInbox(inboxOwner).get(0);
    }

    private void registerJohnAndJane() {
        john = registerUser("john");
        jane = registerUser("jane");
    }

    private void sendToInbox(UserID from, UserID to, DSDocument document) {
        adapter.writeDocumentToInboxOfUser(document, to, document.getPath());
    }

    private UserIDAuth registerUser(String userName) {
        UserIDAuth auth = new UserIDAuth();
        auth.setUserID(new UserID(userName));
        auth.setReadKeyPassword(new ReadKeyPassword("secure-password " + userName));

        adapter.createUser(auth);

        return auth;
    }
}
