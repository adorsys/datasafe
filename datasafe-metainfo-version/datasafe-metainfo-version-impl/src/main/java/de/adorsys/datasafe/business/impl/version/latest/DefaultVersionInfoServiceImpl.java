package de.adorsys.datasafe.business.impl.version.latest;

import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.resource.*;
import de.adorsys.datasafe.business.api.version.VersionEncoder;
import de.adorsys.datasafe.business.api.version.VersionInfoService;
import de.adorsys.datasafe.business.impl.privatespace.actions.ListPrivate;
import de.adorsys.datasafe.business.impl.version.types.DFSVersion;

import javax.inject.Inject;
import java.util.Objects;
import java.util.stream.Stream;

public class DefaultVersionInfoServiceImpl implements VersionInfoService<DFSVersion> {

    private final VersionEncoder encoder;
    private final ListPrivate listPrivate;
    private final EncryptedLatestLinkServiceImpl latestVersionLinkLocator;

    @Inject
    public DefaultVersionInfoServiceImpl(VersionEncoder encoder, ListPrivate listPrivate,
                                         EncryptedLatestLinkServiceImpl latestVersionLinkLocator) {
        this.encoder = encoder;
        this.listPrivate = listPrivate;
        this.latestVersionLinkLocator = latestVersionLinkLocator;
    }

    @Override
    public Stream<Versioned<AbsoluteLocation<ResolvedResource>, PrivateResource, DFSVersion>> versionsOf(
            ListRequest<UserIDAuth, PrivateResource> request) {

        return listPrivate.list(request).map(this::parseVersion).filter(Objects::nonNull);
    }

    @Override
    public Stream<Versioned<AbsoluteLocation<ResolvedResource>, ResolvedResource, DFSVersion>> listJoinedWithLatest(
            ListRequest<UserIDAuth, PrivateResource> request) {
        return versionsOf(request).map(it -> resolveLatest(request, it));
    }

    private Versioned<AbsoluteLocation<ResolvedResource>, ResolvedResource, DFSVersion> resolveLatest(
            ListRequest<UserIDAuth, PrivateResource> request,
            Versioned<AbsoluteLocation<ResolvedResource>, PrivateResource, DFSVersion> versioned) {
        AbsoluteLocation<PrivateResource> latestLink = latestVersionLinkLocator
                .resolveLatestLinkLocation(request.getOwner(), versioned.stripVersion());

        // TODO: This can be cached - latest links for resource version.
        AbsoluteLocation<ResolvedResource> resolved = listPrivate
                .list(request.toBuilder().location(latestLink.getResource()).build())
                .findFirst().orElse(null);

        // no latest found - it is possibly outdated, user needs to validate timestamp.
        if (null == resolved) {
            return new BaseVersionedPath<>(
                    versioned.getVersion(),
                    versioned.absolute(),
                    null
            );
        }

        AbsoluteLocation<PrivateResource> latestObject =
                latestVersionLinkLocator.readLinkAndDecrypt(request.getOwner(), latestLink);
        return new BaseVersionedPath<>(
                versioned.getVersion(),
                versioned.absolute(),
                resolved.getResource().withResource(latestObject.getResource())
        );
    }

    private Versioned<AbsoluteLocation<ResolvedResource>, PrivateResource, DFSVersion> parseVersion(
            AbsoluteLocation<ResolvedResource> resource) {
        VersionedUri versionedUri = encoder.decodeVersion(
                resource.getResource().asPrivate().decryptedPath()
        ).orElse(null);

        if (null == versionedUri) {
            return null;
        }

        return new BaseVersionedPath<>(
                new DFSVersion(versionedUri.getVersion()),
                resource,
                BasePrivateResource.forPrivate(versionedUri.getPathWithoutVersion())
        );
    }
}
