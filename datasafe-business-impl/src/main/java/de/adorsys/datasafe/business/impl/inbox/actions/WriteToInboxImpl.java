package de.adorsys.datasafe.business.impl.inbox.actions;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.datasafe.business.api.deployment.credentials.BucketAccessService;
import de.adorsys.datasafe.business.api.deployment.document.DocumentWriteService;
import de.adorsys.datasafe.business.api.deployment.inbox.actions.WriteToInbox;
import de.adorsys.datasafe.business.api.types.inbox.InboxWriteRequest;
import de.adorsys.datasafe.business.api.deployment.keystore.PublicKeyService;
import de.adorsys.datasafe.business.api.deployment.profile.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.types.DFSAccess;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;

import javax.inject.Inject;
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
    public void write(InboxWriteRequest request) {
        DFSAccess userInbox = accessService.publicAccessFor(
                request.getTo(),
                resolveFileLocation(request)
        );

        // TODO: Map from into file meta
        // FIXME "https://github.com/adorsys/datasafe2/issues/<>"
        WriteRequest writeRequest = WriteRequest.builder()
                .to(userInbox)
                .keyWithId(publicKeyService.publicKey(request.getTo()))
                .data(request.getRequest())
                .build();

        writer.write(writeRequest);
    }

    private Function<ProfileRetrievalService, BucketPath> resolveFileLocation(InboxWriteRequest request) {
        return profiles -> profiles
                .publicProfile(request.getTo())
                .getInbox()
                // TODO: UUID based unique filename
                // FIXME "https://github.com/adorsys/datasafe2/issues/<>"
                .append(request.getRequest().getMeta().getName());
    }
}
