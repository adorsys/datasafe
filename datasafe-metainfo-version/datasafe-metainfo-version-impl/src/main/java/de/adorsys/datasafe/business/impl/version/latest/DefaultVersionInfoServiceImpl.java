package de.adorsys.datasafe.business.impl.version.latest;

import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.resource.*;
import de.adorsys.datasafe.business.api.version.VersionEncoder;
import de.adorsys.datasafe.business.api.version.VersionInfoService;
import de.adorsys.datasafe.business.impl.privatespace.actions.EncryptedResourceResolver;
import de.adorsys.datasafe.business.impl.privatespace.actions.ListPrivate;
import de.adorsys.datasafe.business.impl.version.types.DFSVersion;

import javax.inject.Inject;
import java.util.Objects;
import java.util.stream.Stream;

public class DefaultVersionInfoServiceImpl implements VersionInfoService<DFSVersion> {

    private final VersionEncoder encoder;
    private final ListPrivate listPrivate;
    private final EncryptedResourceResolver resolver;

    @Inject
    public DefaultVersionInfoServiceImpl(VersionEncoder encoder, ListPrivate listPrivate,
                                         EncryptedResourceResolver resolver) {
        this.encoder = encoder;
        this.listPrivate = listPrivate;
        this.resolver = resolver;
    }

    @Override
    public Stream<Versioned<AbsoluteLocation<PrivateResource>, PrivateResource, DFSVersion>> versionsOf(
            UserIDAuth forUser,
            PrivateResource resource) {
        AbsoluteLocation<PrivateResource> latestLocation = resolver.encryptAndResolvePath(forUser, resource);

        return listPrivate.list(
                ListRequest.<UserIDAuth, PrivateResource>builder()
                        .owner(forUser)
                        .location(latestLocation.getResource())
                        .build()
        ).map(this::parseVersion).filter(Objects::nonNull);
    }

    private Versioned<AbsoluteLocation<PrivateResource>, PrivateResource, DFSVersion> parseVersion(
            AbsoluteLocation<PrivateResource> resource) {
        VersionedUri versionedUri = encoder.decodeVersion(resource.getResource().decryptedPath()).orElse(null);

        if (null == versionedUri) {
            return null;
        }

        return new BaseVersionedPath<>(
                new DFSVersion(versionedUri.getVersion()),
                BasePrivateResource.forPrivate(versionedUri.getPathWithoutVersion()),
                resource
        );
    }
}
