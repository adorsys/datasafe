package de.adorsys.datasafe.privatestore.impl.actions;

import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.privatestore.api.PasswordClearingOutputStream;
import de.adorsys.datasafe.privatestore.api.actions.EncryptedResourceResolver;
import de.adorsys.datasafe.privatestore.api.actions.WriteToPrivate;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.callback.ResourceWriteCallback;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.WithCallback;

import javax.inject.Inject;

/**
 * Default privatespace resource write action that retrieves users' secret key for document encryption
 * then resolves resource location using {@link EncryptedResourceResolver} to get absolute encrypted path
 * and writes+encrypts stream to the returned resource using {@link EncryptedDocumentWriteService}
 */
@RuntimeDelegate
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
    public PasswordClearingOutputStream write(WriteRequest<UserIDAuth, PrivateResource> request) {
        // Access check is explicit (will fail on bad keystore id):
        SecretKeyIDWithKey keySpec = privateKeyService.documentEncryptionSecretKey(request.getOwner());

        return new PasswordClearingOutputStream(writer.write(
                WithCallback.<AbsoluteLocation<PrivateResource>, ResourceWriteCallback>builder()
                        .wrapped(
                            resolver.encryptAndResolvePath(
                                request.getOwner(),
                                request.getLocation(),
                                request.getStorageIdentifier())
                        ).callbacks(request.getCallbacks()).build(),
                keySpec
        ), request.getOwner().getReadKeyPassword());
    }
}
