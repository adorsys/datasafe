package de.adorsys.datasafe.business.impl.version.latest.actions;

import de.adorsys.datasafe.business.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.UserPrivateProfile;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.version.actions.VersionedRead;
import de.adorsys.datasafe.business.impl.privatespace.PrivateSpaceService;
import de.adorsys.datasafe.business.impl.version.EncryptedLatestLinkServiceImpl;
import de.adorsys.datasafe.business.impl.version.types.LatestDFSVersion;
import lombok.Getter;

import javax.inject.Inject;
import java.io.InputStream;

public class LatestReadImpl<V extends LatestDFSVersion> implements VersionedRead<V> {

    @Getter
    private final V strategy;

    private final ProfileRetrievalService profiles;
    private final PrivateSpaceService privateSpace;
    private final EncryptedLatestLinkServiceImpl latestVersionLinkLocator;

    @Inject
    public LatestReadImpl(V versionStrategy, ProfileRetrievalService profiles, PrivateSpaceService privateSpace,
                          EncryptedLatestLinkServiceImpl latestVersionLinkLocator) {
        this.strategy = versionStrategy;
        this.profiles = profiles;
        this.privateSpace = privateSpace;
        this.latestVersionLinkLocator = latestVersionLinkLocator;
    }

    @Override
    public InputStream read(ReadRequest<UserIDAuth, PrivateResource> request) {
        UserPrivateProfile privateProfile = profiles.privateProfile(request.getOwner());

        AbsoluteResourceLocation<PrivateResource> latestSnapshotLink =
                latestVersionLinkLocator.resolveLatestLinkLocation(
                        request.getOwner(),
                        request.getLocation(),
                        privateProfile
                );

        return privateSpace.read(request.toBuilder()
                .location(latestVersionLinkLocator.readLinkAndDecrypt(
                        request.getOwner(),
                        latestSnapshotLink,
                        privateProfile).getResource()
                )
                .build()
        );
    }
}
