package de.adorsys.datasafe.business.api;

import de.adorsys.datasafe.business.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.types.DFSVersion;
import de.adorsys.datasafe.business.impl.privatespace.PrivateSpaceService;

import javax.inject.Inject;

public class VersioningServiceImpl implements VersioningService<DFSVersion> {

    private final ProfileRetrievalService profiles;
    private final PrivateSpaceService privateSpaceService;

    @Inject
    public VersioningServiceImpl(PrivateSpaceService privateSpaceService, ProfileRetrievalService profiles) {
        this.privateSpaceService = privateSpaceService;
        this.profiles = profiles;
    }

    @Override
    public VersionedPrivateSpaceService<DFSVersion> privateSpace(DFSVersion version) {
        if (!DFSVersion.LATEST.equals(version)) {
            throw new IllegalArgumentException("Version handling for non latest objects is not implemented");
        }

        return new LatestVersionedPrivateSpaceServiceImpl(DFSVersion.LATEST, profiles, privateSpaceService);
    }
}
