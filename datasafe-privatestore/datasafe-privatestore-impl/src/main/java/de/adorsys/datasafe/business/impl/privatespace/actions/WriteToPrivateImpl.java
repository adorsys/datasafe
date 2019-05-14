package de.adorsys.datasafe.business.impl.privatespace.actions;

import de.adorsys.datasafe.business.api.encryption.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.business.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.business.api.version.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.version.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;

import javax.inject.Inject;
import java.io.OutputStream;

public class WriteToPrivateImpl implements WriteToPrivate {

    private final PrivateKeyService privateKeyService;
    private final EncryptedResourceResolver resolver;
    private final EncryptedDocumentWriteService writer;

    @Inject
    public WriteToPrivateImpl(PrivateKeyService privateKeyService, EncryptedResourceResolver resolver,
                              EncryptedDocumentWriteService writer) {
        this.privateKeyService = privateKeyService;
        this.resolver = resolver;
        this.writer = writer;
    }

    @Override
    public OutputStream write(WriteRequest<UserIDAuth, PrivateResource> request) {
        SecretKeyIDWithKey keySpec = privateKeyService.documentEncryptionSecretKey(request.getOwner());

        return writer.write(
                resolver.encryptAndResolvePath(request.getOwner(), request.getLocation()),
                keySpec
        );
    }
}
