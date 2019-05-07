package de.adorsys.datasafe.business.impl.privatespace.actions;

import de.adorsys.datasafe.business.api.encryption.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.business.api.encryption.keystore.KeyStoreService;
import de.adorsys.datasafe.business.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;

import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.io.OutputStream;

import static de.adorsys.datasafe.business.api.types.keystore.KeyStoreCreationConfig.SYMM_KEY_ID;

public class WriteToPrivateImpl implements WriteToPrivate {

    private final KeyStoreService keyStoreService;
    private final PrivateKeyService privateKeyService;
    private final EncryptedResourceResolver resolver;
    private final EncryptedDocumentWriteService writer;

    @Inject
    public WriteToPrivateImpl(KeyStoreService keyStoreService,
                              PrivateKeyService privateKeyService, EncryptedResourceResolver resolver,
                              EncryptedDocumentWriteService writer) {
        this.keyStoreService = keyStoreService;
        this.privateKeyService = privateKeyService;
        this.resolver = resolver;
        this.writer = writer;
    }

    @Override
    public OutputStream write(WriteRequest<UserIDAuth, PrivateResource> request) {
        SecretKeySpec keySpec = keyStoreService.getSecretKey(
                privateKeyService.keystore(request.getOwner()),
                SYMM_KEY_ID
        );

        return writer.write(
                resolver.encryptAndResolvePath(request.getOwner(), request.getLocation()),
                new SecretKeyIDWithKey(SYMM_KEY_ID, keySpec)
        );
    }
}
