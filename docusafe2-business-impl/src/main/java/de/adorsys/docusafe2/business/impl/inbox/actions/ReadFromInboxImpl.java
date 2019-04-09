package de.adorsys.docusafe2.business.impl.inbox.actions;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.docusafe2.business.api.document.DocumentReadService;
import de.adorsys.docusafe2.business.api.inbox.actions.ReadFromInbox;
import de.adorsys.docusafe2.business.api.inbox.dto.InboxReadRequest;
import de.adorsys.docusafe2.business.api.keystore.PrivateKeyService;
import de.adorsys.docusafe2.business.api.profile.UserProfileService;
import de.adorsys.docusafe2.business.api.types.DFSAccess;
import de.adorsys.docusafe2.business.api.types.ReadRequest;
import de.adorsys.docusafe2.business.impl.credentials.BucketAccessServiceImpl;

import javax.inject.Inject;
import java.util.function.Function;

public class ReadFromInboxImpl implements ReadFromInbox {

    private final PrivateKeyService privateKeyService;
    private final BucketAccessServiceImpl accessService;
    private final DocumentReadService reader;

    @Inject
    public ReadFromInboxImpl(
            PrivateKeyService privateKeyService, BucketAccessServiceImpl accessService, DocumentReadService reader
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
