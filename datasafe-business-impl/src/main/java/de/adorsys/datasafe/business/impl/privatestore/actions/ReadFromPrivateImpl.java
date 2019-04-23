package de.adorsys.datasafe.business.impl.privatestore.actions;

import de.adorsys.datasafe.business.api.directory.privatespace.actions.ReadFromPrivate;
import de.adorsys.datasafe.business.api.encryption.document.EncryptedDocumentReadService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;
import de.adorsys.datasafe.business.impl.resource.ResourceResolver;

import javax.inject.Inject;
import java.io.InputStream;

public class ReadFromPrivateImpl implements ReadFromPrivate {

    private final ResourceResolver resolver;
    private final EncryptedDocumentReadService reader;

    @Inject
    public ReadFromPrivateImpl(ResourceResolver resolver, EncryptedDocumentReadService reader) {
        this.resolver = resolver;
        this.reader = reader;
    }

    @Override
    public InputStream read(ReadRequest<UserIDAuth> request) {
        return reader.read(resolveRelative(request));
    }

    private ReadRequest<UserIDAuth> resolveRelative(ReadRequest<UserIDAuth> request) {
        return request.toBuilder().location(
                resolver.resolveRelativeToPrivate(request.getOwner(), request.getLocation())
        ).build();
    }
}
