package de.adorsys.docusafe2.business.impl.credentials;

import dagger.Binds;
import dagger.Module;
import de.adorsys.docusafe2.business.api.credentials.BucketAccessService;
import de.adorsys.docusafe2.business.api.credentials.DFSCredentialsService;
import de.adorsys.docusafe2.business.api.keystore.PrivateKeyService;
import de.adorsys.docusafe2.business.api.keystore.PublicKeyService;

@Module
public abstract class DefaultCredentialsModule {

    @Binds
    abstract BucketAccessService bucketAccessService(BucketAccessServiceImpl impl);

    @Binds
    abstract PublicKeyService publicKeyService(PublicKeyServiceImpl impl);

    @Binds
    abstract PrivateKeyService privateKeyService(PrivateKeyServiceImpl impl);

    @Binds
    abstract DFSCredentialsService dfsCredentialsService(DFSCredentialsServiceImpl impl);
}
