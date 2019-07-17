package de.adorsys.datasafe.inbox.impl.actions;

import de.adorsys.datasafe.directory.api.profile.keys.PublicKeyService;
import de.adorsys.datasafe.directory.api.resource.ResourceResolver;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentReadService;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.inbox.api.actions.WriteToInbox;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.PublicResource;

import javax.inject.Inject;
import java.io.OutputStream;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default implementation for stream writing that resolves incoming resource path using
 * {@link ResourceResolver} against each recipients' INBOX and then writes and asymmetrically encrypts data into it
 * using {@link EncryptedDocumentReadService}
 */
@RuntimeDelegate
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

    /**
     * Shares document with multiple recipients so that each of them can read it using his private key.
     * @param request Where to write stream (location can be relative/absolute)
     * @return Stream sink for data to be shared and encrypted
     */
    @Override
    public OutputStream write(WriteRequest<Set<UserID>, PublicResource> request) {
        // No access check - anyone who knows user id can send a message to that user
        return writer.write(
                request.getOwner().stream().collect(Collectors.toMap(
                        publicKeyService::publicKey,
                        it -> resolver.resolveRelativeToPublicInbox(it, request.getLocation())
                ))
        );
    }
}
