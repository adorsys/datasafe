package de.adorsys.datasafe.inbox.impl.actions;

import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.directory.api.profile.keys.PublicKeyService;
import de.adorsys.datasafe.directory.api.resource.ResourceResolver;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentReadService;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.inbox.api.actions.WriteToInbox;
import de.adorsys.datasafe.types.api.actions.WriteInboxRequest;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.BasePublicResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;

import javax.inject.Inject;
import java.io.OutputStream;
import java.net.URI;
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

    private final PrivateKeyService privateKeyService;
    private final ResourceResolver resolver;
    private final EncryptedDocumentWriteService writer;
    private final String rootBucket;

    @Inject
    public WriteToInboxImpl(PublicKeyService publicKeyService, PrivateKeyService privateKeyService, ResourceResolver resolver,
                            EncryptedDocumentWriteService writer,  String rootBucket) {
        this.publicKeyService = publicKeyService;
        this.privateKeyService = privateKeyService;
        this.resolver = resolver;
        this.writer = writer;
        this.rootBucket = rootBucket;
    }

    /**
     * Shares document with multiple recipients so that each of them can read it using his private key.
     * @param request Where to write stream (location can be relative/absolute)
     * @return Stream sink for data to be shared and encrypted
     */
    @Override
    public OutputStream write(WriteInboxRequest<UserIDAuth, Set<UserID>, PublicResource> request) {
        URI location = request.getLocation().location().asURI();
        String bucket = rootBucket != null ? rootBucket : "";
        if (!location.getPath().startsWith(bucket)) {
            location = URI.create(bucket + "/" + location.getPath());
        }
        final URI finalLocation = location;
        return writer.write(
                request.getRecipients().stream().collect(Collectors.toMap(
                        publicKeyService::publicKey,
                        it -> resolver.resolveRelativeToPublicInbox(it, new BasePublicResource(finalLocation))
                )),
                privateKeyService.getKeyPair(request.getOwner())
        );
    }
}
