package de.adorsys.datasafe.business.impl.privatestore.actions;

import de.adorsys.datasafe.business.api.directory.privatespace.actions.WriteToPrivate;
import de.adorsys.datasafe.business.api.encryption.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;

import javax.inject.Inject;
import java.io.OutputStream;

public class WriteToPrivateImpl implements WriteToPrivate {

    private final EncryptedDocumentWriteService writer;

    @Inject
    public WriteToPrivateImpl(EncryptedDocumentWriteService writer) {
        this.writer = writer;
    }

    @Override
    public OutputStream write(WriteRequest<UserIDAuth> request) {
        return writer.write(WriteRequest.<UserID>builder()
                .location(request.getLocation())
                .owner(request.getOwner().getUserID())
                .build()
        );
    }
}
