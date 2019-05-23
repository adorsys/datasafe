package de.adorsys.datasafe.business.impl.version.latest.actions;

import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.actions.RemoveRequest;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteLocation;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.version.actions.VersionedRemove;
import de.adorsys.datasafe.business.api.privatespace.actions.RemoveFromPrivate;
import de.adorsys.datasafe.business.impl.version.latest.EncryptedLatestLinkServiceImpl;
import de.adorsys.datasafe.business.impl.version.types.LatestDFSVersion;
import lombok.Getter;

import javax.inject.Inject;

public class LatestRemoveImpl<V extends LatestDFSVersion> implements VersionedRemove<V> {

    @Getter
    private final V strategy;

    private final RemoveFromPrivate removeFromPrivate;
    private final EncryptedLatestLinkServiceImpl latestVersionLinkLocator;

    @Inject
    public LatestRemoveImpl(V versionStrategy, RemoveFromPrivate removeFromPrivate,
                            EncryptedLatestLinkServiceImpl latestVersionLinkLocator) {
        this.strategy = versionStrategy;
        this.removeFromPrivate = removeFromPrivate;
        this.latestVersionLinkLocator = latestVersionLinkLocator;
    }

    @Override
    public void remove(RemoveRequest<UserIDAuth, PrivateResource> request) {
        AbsoluteLocation<PrivateResource> latestSnapshotLink =
                latestVersionLinkLocator.resolveLatestLinkLocation(
                        request.getOwner(),
                        request.getLocation()
                );

        removeFromPrivate.remove(request.toBuilder()
                .location(latestSnapshotLink.getResource())
                .build()
        );
    }
}
