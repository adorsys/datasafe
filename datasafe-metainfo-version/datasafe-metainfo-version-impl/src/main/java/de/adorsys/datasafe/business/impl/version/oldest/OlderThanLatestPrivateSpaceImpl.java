package de.adorsys.datasafe.business.impl.version.oldest;

import de.adorsys.datasafe.business.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.types.action.RemoveRequest;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteLocation;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.Version;
import de.adorsys.datasafe.business.api.types.resource.Versioned;
import de.adorsys.datasafe.business.api.version.VersionedPrivateSpaceService;
import de.adorsys.datasafe.business.impl.privatespace.PrivateSpaceService;
import de.adorsys.datasafe.business.impl.version.types.OlderThanLatestDFSVersion;
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
    private final V strategy;

    private final ProfileRetrievalService profiles;
    private final PrivateSpaceService privateSpaceService;

    @Override
    public Stream<AbsoluteLocation<PrivateResource>> list(ListRequest<UserIDAuth, PrivateResource> request) {
        // profiles.privateProfile(request.getOwner()).getDocumentVersionStorage()
        return null;
    }

    @Override
    public Stream<Versioned<AbsoluteLocation<PrivateResource>, PrivateResource, Version>> listWithDetails(
            ListRequest<UserIDAuth, PrivateResource> request) {
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

    @Override
    public void remove(RemoveRequest<UserIDAuth, PrivateResource> request) {
    }
}
