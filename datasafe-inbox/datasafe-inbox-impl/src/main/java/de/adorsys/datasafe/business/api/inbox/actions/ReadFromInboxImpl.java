package de.adorsys.datasafe.business.api.inbox.actions;

import de.adorsys.datasafe.business.api.encryption.document.EncryptedDocumentReadService;
import de.adorsys.datasafe.business.api.resource.ResourceResolver;
import de.adorsys.datasafe.business.api.version.types.UserIDAuth;
import de.adorsys.datasafe.business.api.version.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.version.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.version.types.resource.PrivateResource;

import javax.inject.Inject;
import java.io.InputStream;

public class ReadFromInboxImpl implements ReadFromInbox {

    private final ResourceResolver resolver;
    private final EncryptedDocumentReadService reader;

    @Inject
    public ReadFromInboxImpl(ResourceResolver resolver, EncryptedDocumentReadService reader) {
        this.resolver = resolver;
        this.reader = reader;
    }

    @Override
    public InputStream read(ReadRequest<UserIDAuth, PrivateResource> request) {
        return reader.read(resolveRelative(request));
    }

    private ReadRequest<UserIDAuth, AbsoluteResourceLocation<PrivateResource>> resolveRelative
            (ReadRequest<UserIDAuth, PrivateResource> request) {
        return ReadRequest.<UserIDAuth, AbsoluteResourceLocation<PrivateResource>>builder()
                .location(resolver.resolveRelativeToPrivateInbox(
                        request.getOwner(),
                        request.getLocation())
                )
                .owner(request.getOwner())
                .build();
    }
}
