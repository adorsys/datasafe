package de.adorsys.datasafe.business.impl.privatespace.actions;

import de.adorsys.datasafe.business.api.encryption.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;

import javax.inject.Inject;
import java.io.OutputStream;

public class WriteToPrivateImpl implements WriteToPrivate {

    private final EncryptedResourceResolver resolver;
    private final EncryptedDocumentWriteService writer;

    @Inject
    public WriteToPrivateImpl(EncryptedResourceResolver resolver, EncryptedDocumentWriteService writer) {
        this.resolver = resolver;
        this.writer = writer;
    }

    @Override
    public OutputStream write(WriteRequest<UserIDAuth, PrivateResource> request) {
        return writer.write(WriteRequest.<UserID, ResourceLocation>builder()
                .location(resolver.encryptAndResolvePath(request.getOwner(), request.getLocation()))
                .owner(request.getOwner().getUserID())
                .build()
        );
    }
}
