package de.adorsys.datasafe.business.impl.inbox.actions;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.datasafe.business.api.credentials.BucketAccessService;
import de.adorsys.datasafe.business.api.document.DocumentReadService;
import de.adorsys.datasafe.business.api.inbox.actions.ReadFromInbox;
import de.adorsys.datasafe.business.api.inbox.dto.InboxReadRequest;
import de.adorsys.datasafe.business.api.keystore.PrivateKeyService;
import de.adorsys.datasafe.business.api.profile.UserProfileService;
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
    public void readDocumentFromInbox(InboxReadRequest request) {
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

    private Function<UserProfileService, BucketPath> resolveFileLocation(InboxReadRequest request) {
        return profiles -> profiles
                .publicProfile(request.getOwner().getId())
                .getInbox()
                .append(request.getPath());
    }
}
