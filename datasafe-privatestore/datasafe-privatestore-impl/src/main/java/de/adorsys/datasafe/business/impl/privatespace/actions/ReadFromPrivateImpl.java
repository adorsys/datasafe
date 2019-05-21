package de.adorsys.datasafe.business.impl.privatespace.actions;

import de.adorsys.datasafe.business.api.encryption.document.EncryptedDocumentReadService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteLocation;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;

import javax.inject.Inject;
import java.io.InputStream;

public class ReadFromPrivateImpl implements ReadFromPrivate {

    private final EncryptedResourceResolver resolver;
    private final EncryptedDocumentReadService reader;

    @Inject
    public ReadFromPrivateImpl(EncryptedResourceResolver resolver, EncryptedDocumentReadService reader) {
        this.resolver = resolver;
        this.reader = reader;
    }

    @Override
    public InputStream read(ReadRequest<UserIDAuth, PrivateResource> request) {
        return reader.read(resolveRelative(request));
    }

    private ReadRequest<UserIDAuth, AbsoluteLocation<PrivateResource>> resolveRelative(ReadRequest<UserIDAuth, PrivateResource> request) {
        return ReadRequest.<UserIDAuth, AbsoluteLocation<PrivateResource>>builder()
                .owner(request.getOwner())
                .location(resolver.encryptAndResolvePath(
                        request.getOwner(),
                        request.getLocation())
                ).build();
    }
}
