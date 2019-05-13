package de.adorsys.datasafe.business.impl.version.oldest;

import de.adorsys.datasafe.business.api.version.VersionedPrivateSpaceService;
import de.adorsys.datasafe.business.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.impl.version.types.OlderThanLatestDFSVersion;
import de.adorsys.datasafe.business.api.version.types.UserIDAuth;
import de.adorsys.datasafe.business.api.version.types.action.ListRequest;
import de.adorsys.datasafe.business.api.version.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.version.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.version.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.version.types.resource.PrivateResource;
import de.adorsys.datasafe.business.impl.privatespace.PrivateSpaceService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Stream;

/**
 * Use primarily for cleanup.
 * @param <V>
 */
@RequiredArgsConstructor
public class OlderThanLatestPrivateSpaceImpl<V extends OlderThanLatestDFSVersion>
        implements VersionedPrivateSpaceService<V> {

    @Getter
    private final V versionStrategy;

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
