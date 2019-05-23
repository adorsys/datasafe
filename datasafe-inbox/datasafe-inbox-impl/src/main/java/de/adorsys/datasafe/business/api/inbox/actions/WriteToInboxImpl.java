package de.adorsys.datasafe.business.api.inbox.actions;

import de.adorsys.datasafe.business.api.encryption.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.business.api.profile.keys.PublicKeyService;
import de.adorsys.datasafe.business.api.resource.ResourceResolver;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.actions.WriteRequest;
import de.adorsys.datasafe.business.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.business.api.types.resource.PublicResource;

import javax.inject.Inject;
import java.io.OutputStream;

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
