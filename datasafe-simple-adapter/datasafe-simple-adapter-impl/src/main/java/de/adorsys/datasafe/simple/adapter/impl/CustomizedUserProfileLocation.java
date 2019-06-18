package de.adorsys.datasafe.simple.adapter.impl;

import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.directory.impl.profile.config.UserProfileLocation;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.types.api.resource.*;

import java.net.URI;

public class CustomizedUserProfileLocation implements UserProfileLocation {
    private static final Uri PROFILE_ROOT = new Uri("./profiles/");
    private static final Uri PRIVATE_PROFILE = new Uri("private");
    private static final Uri PUBLIC_PROFILE = new Uri("public");
    private Uri systemRoot;

    public CustomizedUserProfileLocation(URI systemRoot) {
        this.systemRoot = DefaultDFSConfig.addTrailingSlashIfNeeded(new Uri(systemRoot));
    }

    public CustomizedUserProfileLocation(String systemRoot) {
        this.systemRoot = DefaultDFSConfig.addTrailingSlashIfNeeded(new Uri(systemRoot));
    }

    private AbsoluteLocation<PublicResource> dfsRoot() {
        return new AbsoluteLocation<>(new BasePublicResource(this.systemRoot));
    }

    public AbsoluteLocation<PrivateResource> locatePrivateProfile(UserID ofUser) {
        return new AbsoluteLocation<>(
                new BasePrivateResource(PROFILE_ROOT.resolve(ofUser.getValue() + "/").resolve(PRIVATE_PROFILE)).resolveFrom(dfsRoot())
        );
    }

    public AbsoluteLocation<PublicResource> locatePublicProfile(UserID ofUser) {
        return new AbsoluteLocation<>(
                new BasePublicResource(PROFILE_ROOT.resolve(ofUser.getValue() + "/").resolve(PUBLIC_PROFILE)).resolveFrom(dfsRoot())
        );
    }
}
