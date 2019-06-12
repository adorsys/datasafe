package de.adorsys.datasafe.directory.impl.profile.config;

import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.api.types.CreateUserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.CreateUserPublicProfile;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadStorePassword;
import de.adorsys.datasafe.types.api.resource.*;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.Collections;

/**
 * Default DFS folders layout provider, suitable both for s3 and filesystem.
 */
@RequiredArgsConstructor
public class DefaultDFSConfig implements DFSConfig {

    private static final String PRIVATE_COMPONENT = "private";
    private static final String PRIVATE_FILES_COMPONENT = PRIVATE_COMPONENT + "/files";
    private static final String PUBLIC_COMPONENT = "public";
    private static final String INBOX_COMPONENT = PUBLIC_COMPONENT + "/" + "inbox";
    private static final String VERSION_COMPONENT = "versions";

    private static final Uri PRIVATE_PROFILE = new Uri("./profiles/private/");
    private static final Uri PUBLIC_PROFILE = new Uri("./profiles/public/");

    private final Uri systemRoot;
    private final ReadStorePassword systemPassword;

    /**
     * @param systemRoot Root location for all files - private files, user profiles, etc. For example you want
     * to place everything in datasafe/system directory within storage
     * @param systemPassword System password to open keystore
     */
    public DefaultDFSConfig(String systemRoot, String systemPassword) {
        this.systemRoot = new Uri(systemRoot);
        this.systemPassword = new ReadStorePassword(systemPassword);
    }

    /**
     * @param systemRoot Root location for all files - private files, user profiles, etc. For example you want
     * to place everything in datasafe/system directory within storage
     * @param systemPassword System password to open keystore
     */
    public DefaultDFSConfig(URI systemRoot, String systemPassword) {
        this.systemRoot = new Uri(systemRoot);
        this.systemPassword = new ReadStorePassword(systemPassword);
    }

    /**
     * @param systemRoot Root location for all files - private files, user profiles, etc. For example you want
     * to place everything in datasafe/system directory within storage
     * @param systemPassword System password to open keystore
     */
    public DefaultDFSConfig(Uri systemRoot, String systemPassword) {
        this.systemRoot = systemRoot;
        this.systemPassword = new ReadStorePassword(systemPassword);
    }

    @Override
    public KeyStoreAuth privateKeyStoreAuth(UserIDAuth auth) {
        return new KeyStoreAuth(
                this.systemPassword,
                auth.getReadKeyPassword()
        );
    }

    @Override
    public AbsoluteLocation publicProfile(UserID forUser) {
        return locatePublicProfile(forUser);
    }

    @Override
    public AbsoluteLocation privateProfile(UserID forUser) {
        return locatePrivateProfile(forUser);
    }

    @Override
    public CreateUserPrivateProfile defaultPrivateTemplate(UserIDAuth id) {
        Uri rootLocation = userRoot(id.getUserID());

        Uri keyStoreUri = rootLocation.resolve("./" + PRIVATE_COMPONENT + "/keystore");
        Uri filesUri = rootLocation.resolve("./" + PRIVATE_FILES_COMPONENT + "/");

        return CreateUserPrivateProfile.builder()
                .id(id)
                .privateStorage(accessPrivate(filesUri))
                .keystore(accessPrivate(keyStoreUri))
                .inboxWithWriteAccess(accessPrivate(inbox(rootLocation)))
                .documentVersionStorage(accessPrivate(rootLocation.resolve("./" + VERSION_COMPONENT + "/")))
                .publishPubKeysTo(access(publicKeys(rootLocation)))
                .associatedResources(Collections.singletonList(accessPrivate(rootLocation)))
                .build();
    }

    @Override
    public CreateUserPublicProfile defaultPublicTemplate(UserIDAuth id) {
        Uri rootLocation = userRoot(id.getUserID());

        return CreateUserPublicProfile.builder()
                .id(id.getUserID())
                .inbox(access(inbox(rootLocation)))
                .publicKeys(access(publicKeys(rootLocation)))
                .build();
    }

    /**
     * Where system files like users' private and public profile are located within DFS.
     */
    private AbsoluteLocation<PublicResource> dfsRoot() {
        return new AbsoluteLocation<>(new BasePublicResource(this.systemRoot));
    }

    private AbsoluteLocation<PrivateResource> locatePrivateProfile(UserID ofUser) {
        return new AbsoluteLocation<>(
                new BasePrivateResource(PRIVATE_PROFILE.resolve(ofUser.getValue())).resolveFrom(dfsRoot())
        );
    }

    private AbsoluteLocation<PublicResource> locatePublicProfile(UserID ofUser) {
        return new AbsoluteLocation<>(
                new BasePublicResource(PUBLIC_PROFILE.resolve(ofUser.getValue())).resolveFrom(dfsRoot())
        );
    }

    private AbsoluteLocation<PublicResource> access(Uri path) {
        return new AbsoluteLocation<>(new BasePublicResource(path));
    }

    private AbsoluteLocation<PrivateResource> accessPrivate(Uri path) {
        return new AbsoluteLocation<>(new BasePrivateResource(path, new Uri(""), new Uri("")));
    }

    private Uri userRoot(UserID auth) {
        return dfsRoot().location().resolve(auth.getValue() + "/");
    }

    private Uri inbox(Uri rootLocation) {
        return rootLocation.resolve("./" + INBOX_COMPONENT + "/");
    }

    private Uri publicKeys(Uri rootLocation) {
        return rootLocation.resolve("./" + PUBLIC_COMPONENT + "/" + "pubkeys");
    }
}
