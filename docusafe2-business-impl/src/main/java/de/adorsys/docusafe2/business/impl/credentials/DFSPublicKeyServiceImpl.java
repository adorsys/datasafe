package de.adorsys.docusafe2.business.impl.credentials;

import de.adorsys.docusafe2.business.api.credentials.BucketAccessService;
import de.adorsys.docusafe2.business.api.keystore.PublicKeyService;
import de.adorsys.docusafe2.business.api.types.PublicKeyWithId;
import de.adorsys.docusafe2.business.api.types.UserId;

import javax.inject.Inject;

/**
 * Retrieves public keystore associated with user from DFS storage.
 */
public class DFSPublicKeyServiceImpl implements PublicKeyService {

    private final BucketAccessService bucketAccessService;

    @Inject
    public DFSPublicKeyServiceImpl(BucketAccessService bucketAccessService) {
        this.bucketAccessService = bucketAccessService;
    }

    @Override
    public PublicKeyWithId publicKey(UserId forUser) {
        return null;
    }
}
