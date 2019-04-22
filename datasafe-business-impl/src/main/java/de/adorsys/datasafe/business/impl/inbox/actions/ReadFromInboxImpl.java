package de.adorsys.datasafe.business.impl.inbox.actions;

import de.adorsys.datasafe.business.api.directory.inbox.actions.ReadFromInbox;
import de.adorsys.datasafe.business.api.directory.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.business.api.encryption.document.DocumentReadService;
import de.adorsys.datasafe.business.api.storage.dfs.BucketAccessService;
import de.adorsys.datasafe.business.api.storage.document.DocumentReadService;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;

import javax.inject.Inject;
import java.io.InputStream;

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
    public InputStream read(ReadRequest request) {
        PrivateResource userInbox = accessService.privateAccessFor(
                request.getOwner(),
                request.getFrom()
        );

        return reader.read(userInbox);
    }
}
