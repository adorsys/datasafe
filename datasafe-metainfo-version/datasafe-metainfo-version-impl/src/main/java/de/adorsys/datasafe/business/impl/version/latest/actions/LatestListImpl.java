package de.adorsys.datasafe.business.impl.version.latest.actions;

import de.adorsys.datasafe.business.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.UserPrivateProfile;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.resource.*;
import de.adorsys.datasafe.business.api.version.actions.VersionedList;
import de.adorsys.datasafe.business.impl.privatespace.PrivateSpaceService;
import de.adorsys.datasafe.business.impl.version.EncryptedLatestLinkServiceImpl;
import de.adorsys.datasafe.business.impl.version.types.LatestDFSVersion;
import lombok.Getter;

import javax.inject.Inject;
import java.util.stream.Stream;

public class LatestListImpl<V extends LatestDFSVersion> implements VersionedList<V> {

    @Getter
    private final V strategy;

    private final ProfileRetrievalService profiles;
    private final PrivateSpaceService privateSpace;
    private final EncryptedLatestLinkServiceImpl latestVersionLinkLocator;

    @Inject
    public LatestListImpl(V versionStrategy, ProfileRetrievalService profiles, PrivateSpaceService privateSpace,
                          EncryptedLatestLinkServiceImpl latestVersionLinkLocator) {
        this.strategy = versionStrategy;
        this.profiles = profiles;
        this.privateSpace = privateSpace;
        this.latestVersionLinkLocator = latestVersionLinkLocator;
    }

    @Override
    public Stream<AbsoluteLocation<PrivateResource>> list(ListRequest<UserIDAuth, PrivateResource> request) {
        return listVersioned(request).map(Versioned::absolute);
    }

    @Override
    public Stream<Versioned<AbsoluteLocation<PrivateResource>, PrivateResource, Version>> listVersioned(
            ListRequest<UserIDAuth, PrivateResource> request) {
        UserPrivateProfile privateProfile = profiles.privateProfile(request.getOwner());

        ListRequest<UserIDAuth, PrivateResource> forLatestSnapshotDir = request.toBuilder().location(
                request.getLocation().resolve(privateProfile.getDocumentVersionStorage().getResource())
        ).build();

        return privateSpace
                .list(forLatestSnapshotDir)
                .map(it -> new BaseVersionedPath<>(
                        new BaseVersion(),
                        it.getResource(),
                        latestVersionLinkLocator.readLinkAndDecrypt(request.getOwner(), it, privateProfile)
                ));
    }
}
