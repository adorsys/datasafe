package de.adorsys.datasafe.business.impl.version.latest.actions;

import de.adorsys.datasafe.business.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.UserPrivateProfile;
import de.adorsys.datasafe.business.api.types.action.RemoveRequest;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteLocation;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.version.actions.VersionedRemove;
import de.adorsys.datasafe.business.impl.privatespace.PrivateSpaceService;
import de.adorsys.datasafe.business.impl.version.EncryptedLatestLinkServiceImpl;
import de.adorsys.datasafe.business.impl.version.types.LatestDFSVersion;
import lombok.Getter;

import javax.inject.Inject;

public class LatestRemoveImpl<V extends LatestDFSVersion> implements VersionedRemove<V> {

    @Getter
    private final V strategy;

    private final ProfileRetrievalService profiles;
    private final PrivateSpaceService privateSpace;
    private final EncryptedLatestLinkServiceImpl latestVersionLinkLocator;

    @Inject
    public LatestRemoveImpl(V versionStrategy, ProfileRetrievalService profiles, PrivateSpaceService privateSpace,
                            EncryptedLatestLinkServiceImpl latestVersionLinkLocator) {
        this.strategy = versionStrategy;
        this.profiles = profiles;
        this.privateSpace = privateSpace;
        this.latestVersionLinkLocator = latestVersionLinkLocator;
    }

    @Override
    public void remove(RemoveRequest<UserIDAuth, PrivateResource> request) {
        UserPrivateProfile privateProfile = profiles.privateProfile(request.getOwner());

        AbsoluteLocation<PrivateResource> latestSnapshotLink =
                latestVersionLinkLocator.resolveLatestLinkLocation(
                        request.getOwner(),
                        request.getLocation(),
                        privateProfile
                );

        privateSpace.remove(request.toBuilder()
                .location(latestSnapshotLink.getResource())
                .build()
        );
    }
}
