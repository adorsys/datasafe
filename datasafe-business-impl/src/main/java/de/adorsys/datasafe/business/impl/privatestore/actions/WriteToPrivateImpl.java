package de.adorsys.datasafe.business.impl.privatestore.actions;

import de.adorsys.datasafe.business.api.directory.privatespace.actions.WriteToPrivate;
import de.adorsys.datasafe.business.api.directory.profile.keys.PublicKeyService;
import de.adorsys.datasafe.business.api.encryption.document.DocumentWriteService;
import de.adorsys.datasafe.business.api.encryption.pathencryption.PathEncryption;
import de.adorsys.datasafe.business.api.storage.dfs.BucketAccessService;
import de.adorsys.datasafe.business.api.storage.document.DocumentWriteService;
import de.adorsys.datasafe.business.api.types.action.PrivateWriteRequest;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;

import javax.inject.Inject;
import java.io.OutputStream;

public class WriteToPrivateImpl implements WriteToPrivate {

    private final PublicKeyService publicKeyService;
    private final BucketAccessService accessService;
    private final DocumentWriteService writer;
    private final PathEncryption pathEncryption;

    @Inject
    public WriteToPrivateImpl(PublicKeyService publicKeyService, BucketAccessService accessService,
                              DocumentWriteService writer, PathEncryption pathEncryption) {
        this.publicKeyService = publicKeyService;
        this.accessService = accessService;
        this.writer = writer;
        this.pathEncryption = pathEncryption;
    }

    @Override
    public OutputStream write(PrivateWriteRequest request) {
        PrivateResource userPrivate = accessService.privateAccessFor(
                request.getOwner(),
                request.getTo()
        );

        return writer.write(userPrivate);
    }
}
