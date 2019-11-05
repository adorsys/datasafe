package de.adorsys.datasafe.metainfo.version.impl.version.latest.actions;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.metainfo.version.api.actions.VersionedRead;
import de.adorsys.datasafe.metainfo.version.api.version.EncryptedLatestLinkService;
import de.adorsys.datasafe.metainfo.version.impl.version.types.LatestDFSVersion;
import de.adorsys.datasafe.privatestore.api.PasswordClearingInputStream;
import de.adorsys.datasafe.privatestore.api.actions.ReadFromPrivate;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import lombok.Getter;

import javax.inject.Inject;

/**
 * Default versioned resource reader that resolves latest resource link location using
 * {@link EncryptedLatestLinkService}, follows that link by reading its content to get latest blob
 * location, reads and decrypts latest blob content using {@link ReadFromPrivate}
 * @implNote Reads only from versioned resources - can't be used with ordinary one
 * @param <V> version tag
 */
@RuntimeDelegate
public class LatestReadImpl<V extends LatestDFSVersion> implements VersionedRead<V> {

    @Getter
    private final V strategy;

    private final ReadFromPrivate readFromPrivate;
    private final EncryptedLatestLinkService latestVersionLinkLocator;

    @Inject
    public LatestReadImpl(V versionStrategy, ReadFromPrivate readFromPrivate,
                          EncryptedLatestLinkService latestVersionLinkLocator) {
        this.strategy = versionStrategy;
        this.readFromPrivate = readFromPrivate;
        this.latestVersionLinkLocator = latestVersionLinkLocator;
    }

    @Override
    public PasswordClearingInputStream read(ReadRequest<UserIDAuth, PrivateResource> request) {

        AbsoluteLocation<PrivateResource> latestSnapshotLink =
                latestVersionLinkLocator.resolveLatestLinkLocation(
                        request.getOwner(),
                        request.getLocation(),
                        request.getStorageIdentifier()
                );

        return readFromPrivate.read(request.toBuilder()
                .location(
                        latestVersionLinkLocator
                                .linkDecryptingReader(
                                    request.getOwner(),
                                    request.getStorageIdentifier()
                                ).apply(latestSnapshotLink).getResource()
                )
                .build()
        );
    }
}
