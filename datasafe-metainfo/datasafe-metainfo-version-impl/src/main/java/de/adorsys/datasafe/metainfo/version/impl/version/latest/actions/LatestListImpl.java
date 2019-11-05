package de.adorsys.datasafe.metainfo.version.impl.version.latest.actions;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.metainfo.version.api.actions.VersionedList;
import de.adorsys.datasafe.metainfo.version.api.version.EncryptedLatestLinkService;
import de.adorsys.datasafe.metainfo.version.impl.version.VersionEncoderDecoder;
import de.adorsys.datasafe.metainfo.version.impl.version.types.DFSVersion;
import de.adorsys.datasafe.metainfo.version.impl.version.types.LatestDFSVersion;
import de.adorsys.datasafe.privatestore.api.PasswordClearingStream;
import de.adorsys.datasafe.privatestore.api.actions.ListPrivate;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BaseVersionedPath;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.resource.Version;
import de.adorsys.datasafe.types.api.resource.Versioned;
import de.adorsys.datasafe.types.api.resource.VersionedUri;
import lombok.Getter;

import javax.inject.Inject;
import java.util.Objects;
import java.util.function.Function;

/**
 * Default latest list operation that reads latest resource root for incoming request
 * using {@link EncryptedLatestLinkService}, then lists raw blobs within that root
 * (inside privatespace using {@link ListPrivate}) and parses them into version, logical resource
 * using {@link VersionEncoderDecoder}
 * @implNote Shows only versioned resources, won't show unversioned resources
 * @param <V> version tag
 */
@RuntimeDelegate
public class LatestListImpl<V extends LatestDFSVersion> implements VersionedList<V> {

    @Getter
    private final V strategy;

    private final VersionEncoderDecoder encoder;
    private final ListPrivate listPrivate;
    private final EncryptedLatestLinkService latestVersionLinkLocator;

    @Inject
    public LatestListImpl(V strategy, VersionEncoderDecoder encoder, ListPrivate listPrivate,
                          EncryptedLatestLinkService latestVersionLinkLocator) {
        this.strategy = strategy;
        this.encoder = encoder;
        this.listPrivate = listPrivate;
        this.latestVersionLinkLocator = latestVersionLinkLocator;
    }

    @Override
    public PasswordClearingStream<AbsoluteLocation<ResolvedResource>> list(ListRequest<UserIDAuth, PrivateResource> request) {
        // Returns absolute location of versioned resource tagged with date based on link
        return new PasswordClearingStream(listVersioned(request)
                .map(Versioned::stripVersion)
                .map(AbsoluteLocation::new), request.getOwner().getReadKeyPassword());
    }

    @Override
    public PasswordClearingStream<Versioned<AbsoluteLocation<PrivateResource>, ResolvedResource, Version>> listVersioned(
            ListRequest<UserIDAuth, PrivateResource> request) {

        ListRequest<UserIDAuth, PrivateResource> forLatestSnapshotDir = request.toBuilder().location(
                latestVersionLinkLocator.resolveLatestLinkLocation(
                        request.getOwner(), request.getLocation(), request.getStorageIdentifier()).getResource()
        ).build();

        Function<AbsoluteLocation<PrivateResource>, AbsoluteLocation<PrivateResource>> linkDecryptor =
                latestVersionLinkLocator.linkDecryptingReader(
                    request.getOwner(),
                    request.getStorageIdentifier()
                );

        return new PasswordClearingStream<>(listPrivate
                .list(forLatestSnapshotDir)
                .map(it -> parseVersion(it, linkDecryptor))
                .filter(Objects::nonNull), request.getOwner().getReadKeyPassword());
    }

    private Versioned<AbsoluteLocation<PrivateResource>, ResolvedResource, Version> parseVersion(
            AbsoluteLocation<ResolvedResource> resource,
            Function<AbsoluteLocation<PrivateResource>, AbsoluteLocation<PrivateResource>> linkDecryptor) {

        AbsoluteLocation<PrivateResource> privateBlob = linkDecryptor
                .apply(new AbsoluteLocation<>(resource.getResource().asPrivate()));

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
