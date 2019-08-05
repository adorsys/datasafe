package de.adorsys.datasafe.inbox.impl.actions;

import de.adorsys.datasafe.directory.api.resource.ResourceResolver;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentReadService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.inbox.api.actions.ReadFromInbox;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;

import javax.inject.Inject;
import java.io.InputStream;

/**
 * Default implementation for stream reading that resolves incoming resource path using
 * {@link ResourceResolver} against INBOX and then reads and asymmetrically decrypts data from it
 * using {@link EncryptedDocumentReadService}
 */
@RuntimeDelegate
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
        // Access check is implicit - on keystore access in EncryptedDocumentReadService
        return reader.read(ReadRequest.<UserIDAuth, AbsoluteLocation<PrivateResource>>builder()
                .location(resolver.resolveRelativeToPrivateInbox(request.getOwner(), request.getLocation()))
                .owner(request.getOwner())
                .storageIdentifier(request.getStorageIdentifier())
                .build()
        );
    }
}
