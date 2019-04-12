package de.adorsys.datasafe.business.impl.inbox.actions;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.datasafe.business.api.deployment.credentials.BucketAccessService;
import de.adorsys.datasafe.business.api.deployment.document.DocumentReadService;
import de.adorsys.datasafe.business.api.deployment.inbox.actions.ReadFromInbox;
import de.adorsys.datasafe.business.api.deployment.inbox.dto.InboxReadRequest;
import de.adorsys.datasafe.business.api.deployment.keystore.PrivateKeyService;
import de.adorsys.datasafe.business.api.deployment.profile.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.types.DFSAccess;
import de.adorsys.datasafe.business.api.types.ReadRequest;

import javax.inject.Inject;
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

    private Function<ProfileRetrievalService, BucketPath> resolveFileLocation(InboxReadRequest request) {
        return profiles -> profiles
                .publicProfile(request.getOwner().getUserID())
                .getInbox()
                .append(request.getPath());
    }
}
