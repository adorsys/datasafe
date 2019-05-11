package de.adorsys.datasafe.business.api;

import de.adorsys.datasafe.business.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.types.DFSVersion;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.impl.privatespace.PrivateSpaceService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class LatestVersionedPrivateSpaceServiceImpl implements VersionedPrivateSpaceService<DFSVersion> {

    @Getter
    private final DFSVersion version;

    private final ProfileRetrievalService profiles;
    private final PrivateSpaceService privateSpaceService;

    @Override
    public Stream<AbsoluteResourceLocation<PrivateResource>> list(ListRequest<UserIDAuth, PrivateResource> request) {
        // profiles.privateProfile(request.getOwner()).getDocumentVersionStorage()
        return null;
    }

    @Override
    public InputStream read(ReadRequest<UserIDAuth, PrivateResource> request) {
        return null;
    }

    @Override
    public OutputStream write(WriteRequest<UserIDAuth, PrivateResource> request) {
        return null;
    }
}
