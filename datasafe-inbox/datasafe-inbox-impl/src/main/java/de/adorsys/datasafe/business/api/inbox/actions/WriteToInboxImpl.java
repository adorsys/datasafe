package de.adorsys.datasafe.business.api.inbox.actions;

import de.adorsys.datasafe.business.api.encryption.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.business.api.resource.ResourceResolver;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.PublicResource;

import javax.inject.Inject;
import java.io.OutputStream;

public class WriteToInboxImpl implements WriteToInbox {

    private final ResourceResolver resolver;
    private final EncryptedDocumentWriteService writer;

    @Inject
    public WriteToInboxImpl(ResourceResolver resolver, EncryptedDocumentWriteService writer) {
        this.resolver = resolver;
        this.writer = writer;
    }

    @Override
    public OutputStream write(WriteRequest<UserID, PublicResource> request) {
        return writer.write(resolveRelative(request));
    }

    private WriteRequest<UserID, AbsoluteResourceLocation<?>> resolveRelative
            (WriteRequest<UserID, PublicResource> request) {
        return WriteRequest.<UserID, AbsoluteResourceLocation<?>>builder()
                .location(resolver.resolveRelativeToPublicInbox(
                        request.getOwner(),
                        request.getLocation())
                )
                .owner(request.getOwner())
                .build();
    }
}
