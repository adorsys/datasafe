package de.adorsys.datasafe.business.impl.credentials;

import de.adorsys.datasafe.business.api.credentials.BucketAccessService;
import de.adorsys.datasafe.business.api.keystore.PrivateKeyService;
import de.adorsys.datasafe.business.api.keystore.types.KeyStoreAccess;
import de.adorsys.datasafe.business.api.types.UserIdAuth;

import javax.inject.Inject;

/**
 * Retrieves and opens private keystore associated with user from DFS storage.
 */
public class DFSPrivateKeyServiceImpl implements PrivateKeyService {

    private final BucketAccessService bucketAccessService;

    @Inject
    public DFSPrivateKeyServiceImpl(BucketAccessService bucketAccessService) {
        this.bucketAccessService = bucketAccessService;
    }

    @Override
    public KeyStoreAccess keystore(UserIdAuth forUser) {
        // FIXME "https://github.com/adorsys/datasafe2/issues/<>"
        throw new UnsupportedOperationException("https://github.com/adorsys/datasafe2/issues/<>");
    }
}
