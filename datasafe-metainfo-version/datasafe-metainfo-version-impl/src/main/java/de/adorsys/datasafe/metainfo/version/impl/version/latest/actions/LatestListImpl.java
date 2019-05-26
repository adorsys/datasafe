package de.adorsys.datasafe.metainfo.version.impl.version.latest.actions;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.metainfo.version.api.version.VersionEncoder;
import de.adorsys.datasafe.metainfo.version.api.actions.VersionedList;
import de.adorsys.datasafe.privatestore.api.actions.ListPrivate;
import de.adorsys.datasafe.metainfo.version.impl.version.latest.EncryptedLatestLinkServiceImpl;
import de.adorsys.datasafe.metainfo.version.impl.version.types.DFSVersion;
import de.adorsys.datasafe.metainfo.version.impl.version.types.LatestDFSVersion;
import de.adorsys.datasafe.types.api.resource.*;
import lombok.Getter;

import javax.inject.Inject;
import java.util.Objects;
import java.util.stream.Stream;

public class LatestListImpl<V extends LatestDFSVersion> implements VersionedList<V> {

    @Getter
    private final V strategy;

    private final VersionEncoder encoder;
    private final ListPrivate listPrivate;
    private final EncryptedLatestLinkServiceImpl latestVersionLinkLocator;

    @Inject
    public LatestListImpl(V strategy, VersionEncoder encoder, ListPrivate listPrivate,
                          EncryptedLatestLinkServiceImpl latestVersionLinkLocator) {
        this.strategy = strategy;
        this.encoder = encoder;
        this.listPrivate = listPrivate;
        this.latestVersionLinkLocator = latestVersionLinkLocator;
    }

    @Override
    public Stream<AbsoluteLocation<ResolvedResource>> list(ListRequest<UserIDAuth, PrivateResource> request) {
        // Returns absolute location of versioned resource tagged with date based on link
        return listVersioned(request)
                .map(it -> it.stripVersion().withResource(it.absolute().getResource()))
                .map(AbsoluteLocation::new);
    }

    @Override
    public Stream<Versioned<AbsoluteLocation<PrivateResource>, ResolvedResource, Version>> listVersioned(
            ListRequest<UserIDAuth, PrivateResource> request) {

        ListRequest<UserIDAuth, PrivateResource> forLatestSnapshotDir = request.toBuilder().location(
                latestVersionLinkLocator.resolveLatestLinkLocation(
                        request.getOwner(), request.getLocation()).getResource()
        ).build();

        return listPrivate
                .list(forLatestSnapshotDir)
                .map(it -> parseVersion(request, it))
                .filter(Objects::nonNull);
    }

    private Versioned<AbsoluteLocation<PrivateResource>, ResolvedResource, Version> parseVersion(
            ListRequest<UserIDAuth, PrivateResource> request, AbsoluteLocation<ResolvedResource> resource) {
        AbsoluteLocation<PrivateResource> privateBlob =
                latestVersionLinkLocator.readLinkAndDecrypt(
                        request.getOwner(),
                        new AbsoluteLocation<>(resource.getResource().asPrivate())
                );

        VersionedUri versionedUri = encoder.decodeVersion(privateBlob.getResource().decryptedPath()).orElse(null);

        if (null == versionedUri) {
            return null;
        }

        return new BaseVersionedPath<>(
                new DFSVersion(versionedUri.getVersion()),
                privateBlob,
                resource.getResource()
        );
    }
}
