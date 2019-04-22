package de.adorsys.datasafe.business.impl.privatestore.actions;

import de.adorsys.datasafe.business.api.directory.privatespace.actions.ReadFromPrivate;
import de.adorsys.datasafe.business.api.encryption.document.EncryptedDocumentReadService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;

import javax.inject.Inject;
import java.io.InputStream;

public class ReadFromPrivateImpl implements ReadFromPrivate {

    private final EncryptedDocumentReadService reader;

    @Inject
    public ReadFromPrivateImpl(EncryptedDocumentReadService reader) {
        this.reader = reader;
    }

    @Override
    public InputStream read(ReadRequest<UserIDAuth> request) {
        return reader.read(request);
    }
}
