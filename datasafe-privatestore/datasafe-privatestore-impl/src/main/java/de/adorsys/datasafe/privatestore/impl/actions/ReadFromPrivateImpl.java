package de.adorsys.datasafe.privatestore.impl.actions;

import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentReadService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.privatestore.api.PasswordClearingInputStream;
import de.adorsys.datasafe.privatestore.api.actions.EncryptedResourceResolver;
import de.adorsys.datasafe.privatestore.api.actions.ReadFromPrivate;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;

import javax.inject.Inject;

/**
 * Default implementation for stream reading that encrypts incoming resource path if it is relative using
 * {@link EncryptedResourceResolver} then reads and decrypts data from it using {@link EncryptedDocumentReadService}
 */
@RuntimeDelegate
public class ReadFromPrivateImpl implements ReadFromPrivate {

    private final EncryptedResourceResolver resolver;
    private final EncryptedDocumentReadService reader;

    private ReadKeyPassword readKeyPassword;

    @Inject
    public ReadFromPrivateImpl(EncryptedResourceResolver resolver, EncryptedDocumentReadService reader) {
        this.resolver = resolver;
        this.reader = reader;
    }

    @Override
    public PasswordClearingInputStream read(ReadRequest<UserIDAuth, PrivateResource> request) {
        // Access check is implicit - on keystore access in EncryptedResourceResolver
        return new PasswordClearingInputStream(reader.read(resolveRelative(request)), request.getOwner().getReadKeyPassword());
    }

    private ReadRequest<UserIDAuth, AbsoluteLocation<PrivateResource>> resolveRelative(
            ReadRequest<UserIDAuth, PrivateResource> request) {
        return ReadRequest.<UserIDAuth, AbsoluteLocation<PrivateResource>>builder()
                .owner(request.getOwner())
                .location(resolver.encryptAndResolvePath(
                        request.getOwner(),
                        request.getLocation(),
                        request.getStorageIdentifier()
                ))
                .storageIdentifier(request.getStorageIdentifier())
                .build();
    }

}
