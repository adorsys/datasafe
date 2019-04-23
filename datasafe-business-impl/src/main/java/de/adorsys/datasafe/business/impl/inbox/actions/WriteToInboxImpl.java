package de.adorsys.datasafe.business.impl.inbox.actions;

import de.adorsys.datasafe.business.api.directory.inbox.actions.WriteToInbox;
import de.adorsys.datasafe.business.api.encryption.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.types.resource.PublicResource;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;
import de.adorsys.datasafe.business.impl.resource.ResourceResolver;

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
        return writer.write(WriteRequest.<UserID, ResourceLocation>builder()
                .location((resolver.resolveRelativeToPublicInbox(request.getOwner(), request.getLocation())))
                .owner(request.getOwner())
                .build()
        );
    }
}
