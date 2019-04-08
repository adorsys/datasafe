package de.adorsys.docusafe2.business.impl.inbox.actions;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.docusafe2.business.api.inbox.actions.ReadFromInbox;
import de.adorsys.docusafe2.business.api.inbox.dto.InboxReadRequest;
import de.adorsys.docusafe2.business.api.profile.UserProfileService;
import de.adorsys.docusafe2.business.api.types.DFSAccess;
import de.adorsys.docusafe2.business.impl.credentials.BucketAccessService;
import de.adorsys.docusafe2.business.impl.document.CMSDocumentReadService;
import de.adorsys.docusafe2.business.api.types.ReadRequest;

import javax.inject.Inject;
import java.util.function.Function;

public class ReadFromInboxImpl implements ReadFromInbox {

    private final BucketAccessService accessService;
    private final CMSDocumentReadService reader;

    @Inject
    public ReadFromInboxImpl(BucketAccessService accessService, CMSDocumentReadService reader) {
        this.accessService = accessService;
        this.reader = reader;
    }

    @Override
    public void readDocumentFromInbox(InboxReadRequest request) {
        DFSAccess userInbox = accessService.accessFor(
                request.getOwner(),
                resolveFileLocation(request)
        );

        ReadRequest readRequest = ReadRequest.builder()
                .from(userInbox)
                .response(request.getResponse())
                .build();

        reader.read(readRequest);
    }

    private Function<UserProfileService, BucketPath> resolveFileLocation(InboxReadRequest request) {
        return profiles -> profiles.publicProfile(
                request.getOwner().getId()).getInbox().append(request.getPath()
        );
    }
}
