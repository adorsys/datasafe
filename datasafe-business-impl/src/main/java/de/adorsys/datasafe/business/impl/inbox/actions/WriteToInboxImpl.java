package de.adorsys.datasafe.business.impl.inbox.actions;

import de.adorsys.datasafe.business.api.directory.inbox.actions.WriteToInbox;
import de.adorsys.datasafe.business.api.directory.profile.keys.PublicKeyService;
import de.adorsys.datasafe.business.api.directory.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.encryption.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;
import de.adorsys.datasafe.business.impl.credentials.BucketAccessService;

import javax.inject.Inject;
import java.io.OutputStream;

public class WriteToInboxImpl implements WriteToInbox {

    private final EncryptedDocumentWriteService writer;

    @Inject
    public WriteToInboxImpl(EncryptedDocumentWriteService writer) {
        this.writer = writer;
    }

    @Override
    public OutputStream write(WriteRequest<UserID> request) {
        return writer.write(request);
    }
}
