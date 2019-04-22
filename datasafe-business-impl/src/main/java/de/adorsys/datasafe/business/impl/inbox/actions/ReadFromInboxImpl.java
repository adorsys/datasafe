package de.adorsys.datasafe.business.impl.inbox.actions;

import de.adorsys.datasafe.business.api.directory.inbox.actions.ReadFromInbox;
import de.adorsys.datasafe.business.api.encryption.document.EncryptedDocumentReadService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;

import javax.inject.Inject;
import java.io.InputStream;

public class ReadFromInboxImpl implements ReadFromInbox {

    private final EncryptedDocumentReadService reader;

    @Inject
    public ReadFromInboxImpl(EncryptedDocumentReadService reader) {
        this.reader = reader;
    }

    @Override
    public InputStream read(ReadRequest<UserIDAuth> request) {
        return reader.read(request);
    }
}
