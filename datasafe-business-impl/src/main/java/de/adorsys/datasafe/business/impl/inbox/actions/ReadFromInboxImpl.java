package de.adorsys.datasafe.business.impl.inbox.actions;

import de.adorsys.datasafe.business.api.deployment.credentials.BucketAccessService;
import de.adorsys.datasafe.business.api.deployment.document.DocumentReadService;
import de.adorsys.datasafe.business.api.deployment.inbox.actions.ReadFromInbox;
import de.adorsys.datasafe.business.api.deployment.keystore.PrivateKeyService;
import de.adorsys.datasafe.business.api.deployment.profile.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.types.DFSAccess;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.types.inbox.InboxReadRequest;

import javax.inject.Inject;
import java.net.URI;
import java.util.function.Function;

public class ReadFromInboxImpl implements ReadFromInbox {

    private final PrivateKeyService privateKeyService;
    private final BucketAccessService accessService;
    private final DocumentReadService reader;

    @Inject
    public ReadFromInboxImpl(
            PrivateKeyService privateKeyService, BucketAccessService accessService, DocumentReadService reader
    ) {
        this.privateKeyService = privateKeyService;
        this.accessService = accessService;
        this.reader = reader;
    }

    @Override
    public void read(InboxReadRequest request) {
        DFSAccess userInbox = accessService.privateAccessFor(
                request.getOwner(),
                resolveFileLocation(request)
        );

        ReadRequest readRequest = ReadRequest.builder()
                .from(userInbox)
                .keyStore(privateKeyService.keystore(request.getOwner()))
                .response(request.getResponse())
                .build();

        reader.read(readRequest);
    }

    private Function<ProfileRetrievalService, URI> resolveFileLocation(InboxReadRequest request) {
        return profiles -> profiles
                .publicProfile(request.getOwner().getUserID())
                .getInbox()
                .resolve(request.getPath());
    }
}
