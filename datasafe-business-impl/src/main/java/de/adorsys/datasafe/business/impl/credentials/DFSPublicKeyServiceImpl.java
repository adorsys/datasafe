package de.adorsys.datasafe.business.impl.credentials;

import de.adorsys.datasafe.business.api.deployment.credentials.BucketAccessService;
import de.adorsys.datasafe.business.api.deployment.keystore.PublicKeyService;
import de.adorsys.datasafe.business.api.types.PublicKeyWithId;
import de.adorsys.datasafe.business.api.types.UserID;

import javax.inject.Inject;

// DEPLOYMENT
/**
 * Retrieves and opens public keystore associated with user from DFS storage.
 */
public class DFSPublicKeyServiceImpl implements PublicKeyService {

    private final BucketAccessService bucketAccessService;

    @Inject
    public DFSPublicKeyServiceImpl(BucketAccessService bucketAccessService) {
        this.bucketAccessService = bucketAccessService;
    }

    @Override
    public PublicKeyWithId publicKey(UserID forUser) {
        // FIXME "https://github.com/adorsys/datasafe2/issues/<>"
        throw new UnsupportedOperationException("https://github.com/adorsys/datasafe2/issues/<>");
    }
}
