package de.adorsys.datasafe.directory.impl.profile.config;

import de.adorsys.datasafe.directory.api.types.CreateUserPrivateProfile;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadStorePassword;
import de.adorsys.datasafe.types.api.resource.Uri;

import java.net.URI;

public class DFSConfigWithStorageCreds extends DefaultDFSConfig {

    public DFSConfigWithStorageCreds(Uri systemRoot, ReadStorePassword systemPassword,
                                     UserProfileLocation userProfileLocation) {
        super(systemRoot, systemPassword, userProfileLocation);
    }

    public DFSConfigWithStorageCreds(String systemRoot, ReadStorePassword systemPassword) {
        super(systemRoot, systemPassword);
    }

    public DFSConfigWithStorageCreds(URI systemRoot, ReadStorePassword systemPassword) {
        super(systemRoot, systemPassword);
    }

    public DFSConfigWithStorageCreds(Uri systemRoot, ReadStorePassword systemPassword) {
        super(systemRoot, systemPassword);
    }

    public DFSConfigWithStorageCreds(Uri systemRoot, String systemPassword, UserProfileLocation userProfileLocation) {
        super(systemRoot, systemPassword, userProfileLocation);
    }

    @Override
    public CreateUserPrivateProfile defaultPrivateTemplate(UserIDAuth id) {
        CreateUserPrivateProfile base = super.defaultPrivateTemplate(id);
        Uri rootLocation = userRoot(id.getUserID());
        Uri storageCredsUri = rootLocation.resolve(PRIVATE_COMPONENT + "/storagekeystore");
        return base.toBuilder().storageCredentialsKeystore(accessPrivate(storageCredsUri)).build();
    }
}
