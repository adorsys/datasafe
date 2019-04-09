package de.adorsys.docusafe2.business.impl.credentials;

import de.adorsys.docusafe2.business.api.credentials.BucketAccessService;
import de.adorsys.docusafe2.business.api.keystore.PrivateKeyService;
import de.adorsys.docusafe2.business.api.keystore.types.KeyStoreAccess;
import de.adorsys.docusafe2.business.api.types.UserIdAuth;

import javax.inject.Inject;

public class PrivateKeyServiceImpl implements PrivateKeyService {

    private final BucketAccessService bucketAccessService;

    @Inject
    public PrivateKeyServiceImpl(BucketAccessService bucketAccessService) {
        this.bucketAccessService = bucketAccessService;
    }

    @Override
    public KeyStoreAccess keystore(UserIdAuth forUser) {
        return null;
    }
}
