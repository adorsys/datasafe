package de.adorsys.datasafe.directory.impl.profile.config;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;

import java.net.URI;

public class MultiDFSConfig extends DefaultDFSConfig {

    private final Uri profilesPath;

    public MultiDFSConfig(URI fsPath, URI profilesPath, ReadStorePassword systemPassword) {
        super(fsPath, systemPassword);
        this.profilesPath = new Uri(profilesPath);
    }

    @Override
    public AbsoluteLocation publicProfile(UserID forUser) {
        return new AbsoluteLocation<>(
                BasePrivateResource.forPrivate(
                        profilesPath.resolve("public_profiles/").resolve(forUser.getValue())
                )
        );
    }

    @Override
    public AbsoluteLocation privateProfile(UserID forUser) {
        return new AbsoluteLocation<>(
                BasePrivateResource.forPrivate(
                        profilesPath.resolve("private_profiles/").resolve(forUser.getValue())
                )
        );
    }
}
