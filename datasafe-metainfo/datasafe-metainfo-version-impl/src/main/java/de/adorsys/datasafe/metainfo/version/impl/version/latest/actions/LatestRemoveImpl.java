package de.adorsys.datasafe.metainfo.version.impl.version.latest.actions;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.metainfo.version.api.actions.VersionedRemove;
import de.adorsys.datasafe.metainfo.version.api.version.EncryptedLatestLinkService;
import de.adorsys.datasafe.metainfo.version.impl.version.types.LatestDFSVersion;
import de.adorsys.datasafe.privatestore.api.actions.RemoveFromPrivate;
import de.adorsys.datasafe.types.api.actions.RemoveRequest;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import lombok.Getter;

import javax.inject.Inject;

/**
 * Default versioned resource remove action that simply removes document returned by {@link EncryptedLatestLinkService}
 * so that old versions are preserved, because they are blobs within privatestorage.
 * @implNote Removes only versioned resource - can't be used with ordinary one
 * @param <V> version tag
 */
@RuntimeDelegate
public class LatestRemoveImpl<V extends LatestDFSVersion> implements VersionedRemove<V> {

    @Getter
    private final V strategy;

    private final RemoveFromPrivate removeFromPrivate;
    private final EncryptedLatestLinkService latestVersionLinkLocator;

    @Inject
    public LatestRemoveImpl(V versionStrategy, RemoveFromPrivate removeFromPrivate,
                            EncryptedLatestLinkService latestVersionLinkLocator) {
        this.strategy = versionStrategy;
        this.removeFromPrivate = removeFromPrivate;
        this.latestVersionLinkLocator = latestVersionLinkLocator;
    }

    @Override
    public void remove(RemoveRequest<UserIDAuth, PrivateResource> request) {
        AbsoluteLocation<PrivateResource> latestSnapshotLink =
                latestVersionLinkLocator.resolveLatestLinkLocation(
                        request.getOwner(),
                        request.getLocation(),
                        request.getStorageIdentifier()
                );

        removeFromPrivate.remove(request.toBuilder()
                .location(latestSnapshotLink.getResource())
                .build()
        );
        request.getOwner().getReadKeyPassword().clear();
    }

    @Override
    public void makeSurePasswordClearanceIsDone() {

    }
}
