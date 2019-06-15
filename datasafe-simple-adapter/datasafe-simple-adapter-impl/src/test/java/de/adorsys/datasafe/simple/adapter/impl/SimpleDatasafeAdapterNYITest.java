package de.adorsys.datasafe.simple.adapter.impl;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.simple.adapter.api.SimpleDatasafeService;
import de.adorsys.datasafe.simple.adapter.api.exceptions.SimpleAdapterException;
import de.adorsys.datasafe.simple.adapter.api.types.DSDocument;
import de.adorsys.datasafe.simple.adapter.api.types.DSDocumentStream;
import de.adorsys.datasafe.simple.adapter.api.types.DocumentFQN;
import de.adorsys.datasafe.simple.adapter.api.types.MoveType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SimpleDatasafeAdapterNYITest {
    @Test
    public void nyiTest() {
        SimpleDatasafeService service = new SimpleDatasafeServiceImpl();
        UserIDAuth userIDAuth = new UserIDAuth(new UserID("peter"), new ReadKeyPassword("pass"));
        DocumentFQN doc = new DocumentFQN("a");
        Assertions.assertThrows(SimpleAdapterException.class, () -> service.deleteDocumentFromInbox(userIDAuth, doc));
        Assertions.assertThrows(SimpleAdapterException.class, () -> service.moveDocumentFromInbox(userIDAuth, doc, doc));
        Assertions.assertThrows(SimpleAdapterException.class, () -> service.moveDocumnetToInboxOfUser(userIDAuth, userIDAuth.getUserID(), doc, doc, new MoveType()));
        Assertions.assertThrows(SimpleAdapterException.class, () -> service.registerDFSCredentials(userIDAuth, null));
        Assertions.assertThrows(SimpleAdapterException.class, () -> service.storeDocumentStream(userIDAuth, new DSDocumentStream()));
        Assertions.assertThrows(SimpleAdapterException.class, () -> service.readDocumentStream(userIDAuth, doc));
        Assertions.assertThrows(SimpleAdapterException.class, () -> service.writeDocumentToInboxOfUser(userIDAuth.getUserID(), new DSDocument(null, null), doc));
    }
}
