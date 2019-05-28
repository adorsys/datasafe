package de.adorsys.datasafe.inbox.impl.actions;

import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentReadService;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.directory.api.profile.keys.PublicKeyService;
import de.adorsys.datasafe.directory.api.resource.ResourceResolver;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.types.api.resource.PublicResource;
import de.adorsys.datasafe.inbox.api.actions.WriteToInbox;

import javax.inject.Inject;
import java.io.OutputStream;

/**
 * Default implementation for stream writing that resolves incoming resource path using
 * {@link ResourceResolver} against INBOX and then reads and asymmetrically encrypts data into it
 * using {@link EncryptedDocumentReadService}
 */
public class WriteToInboxImpl implements WriteToInbox {

    private final PublicKeyService publicKeyService;
    private final ResourceResolver resolver;
    private final EncryptedDocumentWriteService writer;

    @Inject
    public WriteToInboxImpl(PublicKeyService publicKeyService, ResourceResolver resolver,
                            EncryptedDocumentWriteService writer) {
        this.publicKeyService = publicKeyService;
        this.resolver = resolver;
        this.writer = writer;
    }

    @Override
    public OutputStream write(WriteRequest<UserID, PublicResource> request) {
        PublicKeyIDWithPublicKey withPublicKey = publicKeyService.publicKey(request.getOwner());
        return writer.write(
                resolver.resolveRelativeToPublicInbox(request.getOwner(), request.getLocation()),
                withPublicKey
        );
    }
}
