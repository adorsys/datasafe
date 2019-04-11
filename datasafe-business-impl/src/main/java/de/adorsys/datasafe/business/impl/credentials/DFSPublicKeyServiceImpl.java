package de.adorsys.datasafe.business.impl.credentials;

import de.adorsys.datasafe.business.api.credentials.BucketAccessService;
import de.adorsys.datasafe.business.api.keystore.PublicKeyService;
import de.adorsys.datasafe.business.api.types.PublicKeyWithId;
import de.adorsys.datasafe.business.api.types.UserId;

import javax.inject.Inject;

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
    public PublicKeyWithId publicKey(UserId forUser) {
        // FIXME "https://github.com/adorsys/datasafe2/issues/<>"
        throw new UnsupportedOperationException("https://github.com/adorsys/datasafe2/issues/<>");
    }
}
