package de.adorsys.datasafe.business.impl.inbox.actions;

import de.adorsys.datasafe.business.api.directory.inbox.actions.WriteToInbox;
import de.adorsys.datasafe.business.api.directory.profile.keys.PublicKeyService;
import de.adorsys.datasafe.business.api.directory.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.encryption.document.DocumentWriteService;
import de.adorsys.datasafe.business.api.storage.dfs.BucketAccessService;
import de.adorsys.datasafe.business.api.storage.document.DocumentWriteService;
import de.adorsys.datasafe.business.api.types.action.InboxWriteRequest;
import de.adorsys.datasafe.business.api.types.resource.PublicResource;

import javax.inject.Inject;
import java.io.OutputStream;

public class WriteToInboxImpl implements WriteToInbox {

    private final PublicKeyService publicKeyService;
    private final BucketAccessService accessService;
    private final DocumentWriteService writer;
    private final ProfileRetrievalService profiles;

    @Inject
    public WriteToInboxImpl(PublicKeyService publicKeyService, BucketAccessService accessService,
                            DocumentWriteService writer, ProfileRetrievalService profiles) {
        this.publicKeyService = publicKeyService;
        this.accessService = accessService;
        this.writer = writer;
        this.profiles = profiles;
    }

    @Override
    public OutputStream write(InboxWriteRequest request) {
        PublicResource userInbox = accessService.publicAccessFor(
                request.getTo(),
                profiles.publicProfile(request.getTo()).getInbox()
        );

        return writer.write(userInbox);
    }
}
