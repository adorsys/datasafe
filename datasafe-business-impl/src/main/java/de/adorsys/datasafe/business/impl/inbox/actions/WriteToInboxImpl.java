package de.adorsys.datasafe.business.impl.inbox.actions;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.docusafe2.business.api.credentials.BucketAccessService;
import de.adorsys.docusafe2.business.api.document.DocumentWriteService;
import de.adorsys.docusafe2.business.api.inbox.actions.WriteToInbox;
import de.adorsys.docusafe2.business.api.inbox.dto.InboxWriteRequest;
import de.adorsys.docusafe2.business.api.keystore.PublicKeyService;
import de.adorsys.docusafe2.business.api.profile.UserProfileService;
import de.adorsys.docusafe2.business.api.types.DFSAccess;
import de.adorsys.docusafe2.business.api.types.WriteRequest;

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
    public void writeDocumentToInboxOfUser(InboxWriteRequest request) {
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

    private Function<UserProfileService, BucketPath> resolveFileLocation(InboxWriteRequest request) {
        return profiles -> profiles
                .publicProfile(request.getTo())
                .getInbox()
                // TODO: UUID based unique filename
                // FIXME "https://github.com/adorsys/datasafe2/issues/<>"
                .append(request.getRequest().getMeta().getName());
    }
}
