package de.adorsys.datasafe.directory.impl.profile.config;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.BasePublicResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;
import de.adorsys.datasafe.types.api.resource.Uri;

public class DefaultUserProfileLocationImpl implements UserProfileLocation {
    private static final Uri PRIVATE_PROFILE = new Uri("./profiles/private/");
    private static final Uri PUBLIC_PROFILE = new Uri("./profiles/public/");

    private final Uri systemRoot;

    public DefaultUserProfileLocationImpl(Uri systemRoot) {
        this.systemRoot = DefaultDFSConfig.addTrailingSlashIfNeeded(systemRoot);
    }

    private AbsoluteLocation<PublicResource> dfsRoot() {
        return new AbsoluteLocation<>(new BasePublicResource(this.systemRoot));
    }

    public AbsoluteLocation<PrivateResource> locatePrivateProfile(UserID ofUser) {
        return new AbsoluteLocation<>(
                new BasePrivateResource(PRIVATE_PROFILE.resolve(ofUser.getValue())).resolveFrom(dfsRoot())
        );
    }

    public AbsoluteLocation<PublicResource> locatePublicProfile(UserID ofUser) {
        return new AbsoluteLocation<>(
                new BasePublicResource(PUBLIC_PROFILE.resolve(ofUser.getValue())).resolveFrom(dfsRoot())
        );
    }
}
