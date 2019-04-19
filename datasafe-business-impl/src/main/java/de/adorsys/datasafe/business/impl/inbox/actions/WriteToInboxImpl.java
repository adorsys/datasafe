package de.adorsys.datasafe.business.impl.inbox.actions;

import de.adorsys.datasafe.business.api.deployment.credentials.BucketAccessService;
import de.adorsys.datasafe.business.api.deployment.document.DocumentWriteService;
import de.adorsys.datasafe.business.api.deployment.inbox.actions.WriteToInbox;
import de.adorsys.datasafe.business.api.deployment.keystore.PublicKeyService;
import de.adorsys.datasafe.business.api.deployment.profile.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.types.DFSAccess;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.types.inbox.InboxWriteRequest;

import javax.inject.Inject;
import java.io.OutputStream;
import java.net.URI;
import java.util.function.Function;

public class WriteToInboxImpl implements WriteToInbox {

    private final PublicKeyService publicKeyService;
    private final BucketAccessService accessService;
    private final DocumentWriteService writer;

    @Inject
    public WriteToInboxImpl(
            PublicKeyService publicKeyService, BucketAccessService accessService, DocumentWriteService writer
    ) {
        this.publicKeyService = publicKeyService;
        this.accessService = accessService;
        this.writer = writer;
    }

    @Override
    public OutputStream write(InboxWriteRequest request) {
        DFSAccess userInbox = accessService.publicAccessFor(
                request.getTo(),
                resolveFileLocation(request)
        );

        // TODO: Map from into file meta
        // FIXME "https://github.com/adorsys/datasafe2/issues/<>"
        WriteRequest writeRequest = WriteRequest.builder()
                .to(userInbox)
                .keyWithId(publicKeyService.publicKey(request.getTo()))
                .build();

        return writer.write(writeRequest);
    }

    private Function<ProfileRetrievalService, URI> resolveFileLocation(InboxWriteRequest request) {
        return profiles -> profiles
                .publicProfile(request.getTo())
                .getInbox()
                // TODO: UUID based unique filename
                // FIXME "https://github.com/adorsys/datasafe2/issues/<>"
                .resolve(request.getRequest().getPath());
    }
}
