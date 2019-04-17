package de.adorsys.datasafe.business.impl.privatestore.actions;

import de.adorsys.datasafe.business.api.deployment.credentials.BucketAccessService;
import de.adorsys.datasafe.business.api.deployment.document.DocumentWriteService;
import de.adorsys.datasafe.business.api.deployment.keystore.PublicKeyService;
import de.adorsys.datasafe.business.api.deployment.privatespace.actions.WriteToPrivate;
import de.adorsys.datasafe.business.api.deployment.profile.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.types.DFSAccess;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.types.privatespace.PrivateWriteRequest;
import de.adorsys.dfs.connection.api.complextypes.BucketPath;

import javax.inject.Inject;
import java.util.function.Function;

public class WriteToPrivateImpl implements WriteToPrivate {

    private final PublicKeyService publicKeyService;
    private final BucketAccessService accessService;
    private final DocumentWriteService writer;

    @Inject
    public WriteToPrivateImpl(
            PublicKeyService publicKeyService, BucketAccessService accessService, DocumentWriteService writer
    ) {
        this.publicKeyService = publicKeyService;
        this.accessService = accessService;
        this.writer = writer;
    }

    @Override
    public void write(PrivateWriteRequest request) {
        DFSAccess userPrivate = accessService.privateAccessFor(
                request.getOwner(),
                resolveFileLocation(request)
        );

        // TODO: Map from into file meta
        // FIXME "https://github.com/adorsys/datasafe2/issues/<>"
        WriteRequest writeRequest = WriteRequest.builder()
                .to(userPrivate)
                .keyWithId(publicKeyService.publicKey(request.getOwner().getUserID()))
                .data(request.getRequest())
                .build();

        writer.write(writeRequest);
    }

    private Function<ProfileRetrievalService, BucketPath> resolveFileLocation(PrivateWriteRequest request) {
        return profiles -> profiles
                .privateProfile(request.getOwner())
                .getPrivateStorage()
                // TODO: Encrypt file name and path
                .append(request.getRequest().getMeta().getName());
    }
}
