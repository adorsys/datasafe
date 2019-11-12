package de.adorsys.datasafe.directory.impl.profile.config;

import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.api.types.CreateUserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.CreateUserPublicProfile;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAuth;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.BasePublicResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;
import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.Collections;
import java.util.function.Supplier;

/**
 * Default DFS folders layout provider, suitable both for s3 and filesystem.
 */
@Slf4j
public class DefaultDFSConfig implements DFSConfig {

    protected static final String USERS_ROOT = "users/";
    protected static final String PRIVATE_COMPONENT = "private";
    protected static final String PRIVATE_FILES_COMPONENT = PRIVATE_COMPONENT + "/files";
    protected static final String PUBLIC_COMPONENT = "public";
    protected static final String INBOX_COMPONENT = PUBLIC_COMPONENT + "/" + "inbox";
    protected static final String VERSION_COMPONENT = "versions";

    protected final Uri systemRoot;
    protected final ReadStorePassword systemPassword;
    protected final UserProfileLocation userProfileLocation;

    /**
     * @param systemRoot Root location for all files - private files, user profiles, etc. For example you want
     * to place everything in datasafe/system directory within storage
     * @param systemPassword System password to open keystore
     */
    public DefaultDFSConfig(String systemRoot, ReadStorePassword systemPassword) {
        this(new Uri(systemRoot), systemPassword);
    }

    /**
     * @param systemRoot Root location for all files - private files, user profiles, etc. For example you want
     * to place everything in datasafe/system directory within storage
     * @param systemPassword System password to open keystore
     */
    public DefaultDFSConfig(String systemRoot, Supplier<char[]> systemPassword) {
        this(new Uri(systemRoot), new ReadStorePassword(systemPassword));
    }


    /**
     * @param systemRoot Root location for all files - private files, user profiles, etc. For example you want
     * to place everything in datasafe/system directory within storage
     * @param systemPassword System password to open keystore
     */
    public DefaultDFSConfig(URI systemRoot, ReadStorePassword systemPassword) {
        this(new Uri(systemRoot), systemPassword);
    }

    /**
     * @param systemRoot Root location for all files - private files, user profiles, etc. For example you want
     * to place everything in datasafe/system directory within storage
     * @param systemPassword System password to open keystore
     */
    public DefaultDFSConfig(URI systemRoot, Supplier<char[]> systemPassword) {
        this(new Uri(systemRoot), new ReadStorePassword(systemPassword));
    }

    /**
     * @param systemRoot Root location for all files - private files, user profiles, etc. For example you want
     * to place everything in datasafe/system directory within storage
     * @param systemPassword System password to open keystore
     */
    public DefaultDFSConfig(Uri systemRoot, ReadStorePassword systemPassword) {
        this(systemRoot, systemPassword, new DefaultUserProfileLocationImpl(systemRoot));
    }

    /**
     * @param systemRoot Root location for all files - private files, user profiles, etc. For example you want
     * to place everything in datasafe/system directory within storage
     * @param systemPassword System password to open keystore
     * @param userProfileLocation Bootstrap for user profile files placement
     */
    public DefaultDFSConfig(Uri systemRoot, ReadStorePassword systemPassword, UserProfileLocation userProfileLocation) {
        systemRoot = addTrailingSlashIfNeeded(systemRoot);
        this.systemRoot = systemRoot;
        this.systemPassword = systemPassword;
        this.userProfileLocation = userProfileLocation;
        log.debug("Root is {}", dfsRoot());
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
        Uri keyStoreUri = rootLocation.resolve(PRIVATE_COMPONENT + "/keystore");
        Uri filesUri = rootLocation.resolve(PRIVATE_FILES_COMPONENT + "/");

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
    public CreateUserPublicProfile defaultPublicTemplate(UserID id) {
        Uri rootLocation = userRoot(id);

        return CreateUserPublicProfile.builder()
                .id(id)
                .inbox(access(inbox(rootLocation)))
                .publicKeys(access(publicKeys(rootLocation)))
                .build();
    }

    protected Uri userRoot(UserID userID) {
        return dfsRoot().location().resolve(USERS_ROOT).resolve(userID.getValue() + "/");
    }

    /**
     * Where system files like users' private and public profile are located within DFS.
     */
    protected AbsoluteLocation<PublicResource> dfsRoot() {
        return new AbsoluteLocation<>(new BasePublicResource(this.systemRoot));
    }

    protected AbsoluteLocation<PrivateResource> locatePrivateProfile(UserID ofUser) {
        return userProfileLocation.locatePrivateProfile(ofUser);
    }

    protected AbsoluteLocation<PublicResource> locatePublicProfile(UserID ofUser) {
        return userProfileLocation.locatePublicProfile(ofUser);
    }

    protected AbsoluteLocation<PublicResource> access(Uri path) {
        return new AbsoluteLocation<>(new BasePublicResource(path));
    }

    protected AbsoluteLocation<PrivateResource> accessPrivate(Uri path) {
        return new AbsoluteLocation<>(new BasePrivateResource(path, new Uri(""), new Uri("")));
    }

    protected Uri inbox(Uri rootLocation) {
        return rootLocation.resolve("./" + INBOX_COMPONENT + "/");
    }

    protected Uri publicKeys(Uri rootLocation) {
        return rootLocation.resolve("./" + PUBLIC_COMPONENT + "/" + "pubkeys");
    }

    public static Uri addTrailingSlashIfNeeded(Uri systemRoot) {
        return new Uri(addTrailingSlashIfNeeded(systemRoot.asURI()));
    }

    @SneakyThrows
    public static URI addTrailingSlashIfNeeded(URI systemRoot) {
        return new URI(addTrailingSlashIfNeeded(systemRoot.toASCIIString()));
    }

    public static String addTrailingSlashIfNeeded(String systemRoot) {
        if (systemRoot == null) {
            throw new IllegalArgumentException("systemRoot must not be null");
        }

        int last = systemRoot.length();

        if (!systemRoot.isEmpty() && systemRoot.substring(last - 1).equals("/")) {
            return systemRoot;
        }

        return systemRoot + "/";
    }
}
